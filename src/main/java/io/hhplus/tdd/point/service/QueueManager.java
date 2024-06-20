package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.repository.PointRepository;
import io.hhplus.tdd.point.domain.TransactionType;
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
  private final PointRepository pointRepository;
  private final ChargeSpecification chargeImpl;
  private final UseSpecification useImpl;

  // * ThreadTaskScheduler를 사용하여 비동기 처리를 위한 스케줄러를 생성합니다.
  private final ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
  // * 스케줄러를 종료하기 위한 ScheduledFuture
  private ScheduledFuture<?> scheduledFuture;
  // * 큐에 있는 요청을 처리하기 위한 임계값
  private static final int QUEUE_THRESHOLD = 10;
  // 요청을 처리하기 위한 ConcurrentLinkedQueue
  private final Queue<QueueEntity> requestQueue = new ConcurrentLinkedQueue<>();
  // 비동기 처리를 위한 CompletableFuture
  private final Map<Long, CompletableFuture<UserPointDTO>> futureMap = new ConcurrentHashMap<>();
  // 비동기 대기를 위한 타임아웃 설정
  private final Duration TIMEOUT = Duration.ofSeconds(10);

  /**
   * QueueManager 생성자
   * @param pointRepository : 포인트 레포지토리, ChargeImpl, UseImpl을 주입받습니다.
   * @param chargeImpl : ChargeImpl, ChargeSpecification에 따른 구현을 주입받습니다.
   * @param useImpl : UseImpl, UseSpecification에 따른 구현을 주입받습니다.
   */
  public QueueManager(PointRepository pointRepository, ChargeImpl chargeImpl, UseImpl useImpl) {
    this.pointRepository = pointRepository;
    this.chargeImpl = chargeImpl;
    this.useImpl = useImpl;
    // * 스케줄러 풀 설정
    taskScheduler.setPoolSize(10);
    // * 스케줄러 이름 설정
    taskScheduler.setThreadNamePrefix("QueueManagerScheduler-");
    // * 스케줄러 초기화
    taskScheduler.initialize();
  }

  /**
   * 스프링 빈이 생성될 때 큐에 있는 요청을 처리하기 위한 스케줄러를 시작합니다.
   */
  @PostConstruct
  public void startProcessing() {
    // * 반복 시간을 설정합니다.
    Duration delay = Duration.ofSeconds(1);
    // * 스케줄러를 시작합니다.
    scheduledFuture = taskScheduler.scheduleWithFixedDelay(this::processQueue, delay);
  }

  /**
   * 스프링 빈이 소멸될 때 스케줄러를 종료합니다.
   */
  @PreDestroy
  public void onDestroy() {
    stopProcessing();
  }

  /**
   * 비동기로 큐에 있는 요청을 처리하기 위한 스케줄러를 종료합니다.
   */
  @Async
  public void stopProcessing() {
    if (scheduledFuture != null) {
      // * 스케줄러를 종료합니다.
      scheduledFuture.cancel(true);
    }
  }

  /**
   * 요청을 비동기로 처리하기위해 요청객체에 미리 콜을 걸어둡니다.
   * @param request : 비동기 처리를 위한 Callable 객체를 받습니다.
   * @return UserPointDTO : 비동기 처리 결과를 반환합니다.
   */
  public UserPointDTO handleRequest(Callable<CompletableFuture<UserPointDTO>> request) {
    try {
      // * 비동기 처리를 위한 Future를 받아 처리합니다.
      CompletableFuture<UserPointDTO> future = request.call();
      // * 비동기 처리 결과를 반환합니다.
      return future.get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
    } catch (ExecutionException | InterruptedException e) {
      // * 에러가 발생하면 스레드를 멈추고 에러메시지를 출력합니다.
      Thread.currentThread().interrupt();
      PointService.errorMessageThrowing("서버 에러가 발생했습니다.");
    } catch (TimeoutException e) {
      PointService.errorMessageThrowing("처리 시간이 초과되었습니다.");
    } catch (Exception e) {
      PointService.errorMessageThrowing("알 수 없는 에러가 발생했습니다.");
    }
    return null;
  }

  /**
   *
   * @param userPointDTO : 큐에 UserDTO를 추가함으로 요청 처리를 위한 준비
   * @param transactionType : 충전 또는 사용 타입을 구분
   * @return CompletableFuture<UserPointDTO> : 비동기 처리를 위한 Future
   */
  public CompletableFuture<UserPointDTO> addToQueue(
      UserPointDTO userPointDTO, TransactionType transactionType) {
    // * 비동기 처리를 위한 Future를 생성합니다.
    CompletableFuture<UserPointDTO> future = new CompletableFuture<>();
    // * 큐에 처리를 위한 요청을 추가합니다.
    requestQueue.offer(new QueueEntity(userPointDTO, transactionType));
    // * Future를 맵에 저장합니다.
    futureMap.put(userPointDTO.id(), future);
    // * 큐에 있는 요청이 임계값을 넘으면 처리합니다.
    if (requestQueue.size() >= QUEUE_THRESHOLD) {
      processQueue();
    }
    // * 비동기 처리를 위한 Future를 반환합니다.
    return future;
  }

  /**
   * 큐에 있는 요청을 처리
   */
  public void processQueue() {
    while (!requestQueue.isEmpty()) {
      // * 큐에 있는 요청을 하나씩 처리합니다.
      QueueEntity request = requestQueue.poll();
      // * 요청이 null이 아닐 경우 요청을 처리합니다.
      if (request != null) {
        processRequest(request);
      }
    }
  }

  /**
   * 큐에 있는 요청을 타입에 따라 구분후 각 스펙에 맞는 구현클래스로 처리요청
   * @param request : 큐에 있는 요청
   */
  private void processRequest(QueueEntity request) {
    try {
      // * 요청 타입에 따라 구분하여 처리합니다.
      if (request.getTransactionType() == TransactionType.CHARGE) {
        // * 충전 요청일 경우 ChargeImpl을 통해 처리합니다.
        chargeImpl.chargeProcess(request.getUserPointDTO());
      } else if (request.getTransactionType() == TransactionType.USE) {
        // * 사용 요청일 경우 UseImpl을 통해 처리합니다.
        useImpl.useProcess(request.getUserPointDTO());
      }
      // * 비동기 처리를 완료합니다.
      completeFuture(request.getUserPointDTO().id());
    } catch (Exception e) {
      // * 에러가 발생하면 스레드를 멈추고 에러메시지를 출력합니다.
      futureMap.get(request.getUserPointDTO().id()).completeExceptionally(e);
    }
  }

  /**
   * 비동기 처리를 위한 Future를 완료
   * @param id : UserPointDTO의 id
   */
  private void completeFuture(long id) {
    // * UserPointDTO의 id를 통해 Future를 가져옵니다.
    Optional<UserPoint> result = pointRepository.selectById(id);
    result.ifPresentOrElse(
            // * UserPointDTO가 존재하면 Future를 완료합니다.
        userPoint -> futureMap.get(id).complete(UserPointDTO.convertToDTO(userPoint)),
        // * UserPointDTO가 존재하지 않으면 에러메시지를 출력합니다.
        () ->
            futureMap
                .get(id)
                .completeExceptionally(new IllegalArgumentException("UserPointDTO is not found")));
  }
}
