package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.repository.PointRepository;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.dto.UserPointDTO;
import io.hhplus.tdd.point.service.charge.ChargeImpl;
import io.hhplus.tdd.point.service.charge.ChargeSpecification;
import io.hhplus.tdd.point.service.use.UseImpl;
import io.hhplus.tdd.point.service.use.UseSpecification;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.*;

@Slf4j
@Component
@EnableAsync
public class QueueManager {
  private final ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
  private ScheduledFuture<?> scheduledFuture;
  private static final int QUEUE_THRESHOLD = 10;
  private final PointRepository pointRepository;
  private final ChargeSpecification chargeImpl;
  private final UseSpecification useImpl;

  // 요청을 처리하기 위한 ConcurrentLinkedQueue
  private final Queue<QueueEntity> requestQueue = new ConcurrentLinkedQueue<>();
  // 비동기 처리를 위한 CompletableFuture
  private final Map<Long, CompletableFuture<UserPointDTO>> futureMap = new ConcurrentHashMap<>();
  // 비동기 대기를 위한 타임아웃 설정
  private final Duration TIMEOUT = Duration.ofSeconds(10);

  public QueueManager(PointRepository pointRepository, ChargeImpl chargeImpl, UseImpl useImpl) {
    this.pointRepository = pointRepository;
    this.chargeImpl = chargeImpl;
    this.useImpl = useImpl;
    taskScheduler.setPoolSize(10);
    taskScheduler.setThreadNamePrefix("QueueManagerScheduler-");
    taskScheduler.initialize();
  }

  @PostConstruct
  public void startProcessing() {
    Duration delay = Duration.ofSeconds(1);
    scheduledFuture = taskScheduler.scheduleWithFixedDelay(this::processQueue, delay);
  }

  @PreDestroy
  public void onDestroy() {
    stopProcessing();
  }

  @Async
  public void stopProcessing() {
    if (scheduledFuture != null) {
      scheduledFuture.cancel(true);
    }
  }

  public UserPointDTO handleRequest(Callable<CompletableFuture<UserPointDTO>> request) {
    try {
      CompletableFuture<UserPointDTO> future = request.call();
      return future.get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
    } catch (ExecutionException | InterruptedException e) {
      Thread.currentThread().interrupt();
      PointService.errorMessageThrowing("서버 에러가 발생했습니다.");
    } catch (TimeoutException e) {
      PointService.errorMessageThrowing("처리 시간이 초과되었습니다.");
    } catch (Exception e) {
      PointService.errorMessageThrowing("알 수 없는 에러가 발생했습니다.");
    }
    return null;
  }

  public CompletableFuture<UserPointDTO> addToQueue(
      UserPointDTO userPointDTO, TransactionType transactionType) {
    CompletableFuture<UserPointDTO> future = new CompletableFuture<>();
    requestQueue.offer(new QueueEntity(userPointDTO, transactionType));
    futureMap.put(userPointDTO.id(), future);
    if (requestQueue.size() >= QUEUE_THRESHOLD) {
      processQueue();
    }
    return future;
  }

  public void processQueue() {
    while (!requestQueue.isEmpty()) {
      QueueEntity request = requestQueue.poll();
      if (request != null) {
        processRequest(request);
      }
    }
  }

  private void processRequest(QueueEntity request) {
    try {
      if (request.getTransactionType() == TransactionType.CHARGE) {
        chargeImpl.chargeProcess(request.getUserPointDTO());
      } else if (request.getTransactionType() == TransactionType.USE) {
        useImpl.useProcess(request.getUserPointDTO());
      }
      completeFuture(request.getUserPointDTO().id());
    } catch (Exception e) {
      futureMap.get(request.getUserPointDTO().id()).completeExceptionally(e);
    }
  }

  private void completeFuture(long id) {
    Optional<UserPoint> result = pointRepository.selectById(id);
    result.ifPresentOrElse(
        userPoint -> futureMap.get(id).complete(UserPointDTO.convertToDTO(userPoint)),
        () ->
            futureMap
                .get(id)
                .completeExceptionally(new IllegalArgumentException("UserPointDTO is not found")));
  }
}
