package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.repository.PointRepository;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.dto.UserPointDTO;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

@Slf4j
@Component
@EnableAsync
public class QueueManager {

  private static final int SCHEDULER_POOL_SIZE = 10;
  private static final String SCHEDULER_THREAD_PREFIX = "QueueManagerScheduler-";
  private final PointRepository pointRepository;

  // * ThreadTaskScheduler를 사용하여 비동기 처리를 위한 스케줄러를 생성합니다.
  private final ThreadPoolTaskScheduler taskScheduler;
  // * 스케줄러를 종료하기 위한 ScheduledFuture
  private ScheduledFuture<?> scheduledFuture;
  // * 큐에 있는 요청을 처리하기 위한 임계값
  private static final int QUEUE_THRESHOLD = 10;
  // 요청을 처리하기 위한 ConcurrentLinkedQueue
  @Getter
  private final Queue<QueueEntity> requestQueue;
  // 비동기 처리를 위한 CompletableFuture
  private final Map<Long, CompletableFuture<UserPointDTO>> futureMap;
  // 비동기 대기를 위한 타임아웃 설정
  private final Duration TIMEOUT = Duration.ofSeconds(10);

  /**
   * QueueManager 생성자
   *
   * @param pointRepository : 포인트 레포지토리, ChargeImpl, UseImpl을 주입받습니다.
   */
  public QueueManager(PointRepository pointRepository) {
    this.pointRepository = pointRepository;
    this.taskScheduler = createTaskScheduler();
    this.requestQueue = new ConcurrentLinkedQueue<>();
    this.futureMap = new ConcurrentHashMap<>();
  }

  private ThreadPoolTaskScheduler createTaskScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(SCHEDULER_POOL_SIZE);
    scheduler.setThreadNamePrefix(SCHEDULER_THREAD_PREFIX);
    scheduler.initialize();
    return scheduler;
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
   *
   * @param request : 비동기 처리를 위한 Callable 객체를 받습니다.
   * @return UserPointDTO : 비동기 처리 결과를 반환합니다.
   */
  public CompletableFuture<UserPointDTO> handleRequest(
      Supplier<CompletableFuture<UserPointDTO>> request) {
    return CompletableFuture.supplyAsync(request)
        .thenCompose(Function.identity())
        .orTimeout(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)
        .exceptionally(e -> {
          if (e instanceof TimeoutException) {
            PointService.errorMessageThrowing("처리 시간이 초과되었습니다.");
          } else if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
            PointService.errorMessageThrowing("서버 에러가 발생했습니다.");
          } else {
            PointService.errorMessageThrowing("알 수 없는 에러가 발생했습니다.");
          }
          return null;
        });
  }

  /**
   * @param userPointDTO    : 큐에 UserDTO를 추가함으로 요청 처리를 위한 준비
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
    futureMap.put(userPointDTO.id().get(), future);
    // * 큐에 있는 요청이 임계값을 넘으면 처리합니다.

    if (requestQueue.size() >= QUEUE_THRESHOLD) {
      processQueue();
    }
    // * 비동기 처리를 위한 Future를 반환합니다.
    return handleRequest(() -> future);
  }

  /**
   * 큐에 있는 요청을 처리
   */
  public void processQueue() {
    while (!requestQueue.isEmpty()) {
      System.out.println(requestQueue.size()+" 큐에 있는 요청 처리");
      QueueEntity request = requestQueue.poll();
      if (request != null) {
        processRequest(request);
      }
    }
  }

  /**
   * 큐에 있는 요청을 타입에 따라 구분후 각 스펙에 맞는 구현클래스로 처리요청
   *
   * @param request : 큐에 있는 요청
   */
  private void processRequest(QueueEntity request) {
    CompletableFuture.supplyAsync(() -> {
          System.out.println("request called = " + request);
          UserPointDTO userPointDTO = request.getUserPointDTO();
          TransactionType transactionType = request.getTransactionType();
          try {
            return switch (transactionType) {
              case CHARGE -> processCharge(userPointDTO);
              case USE -> processUse(userPointDTO);
            };
          } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            throw e;
          } catch (Exception e) {
            log.error("Error processing request: {}", e.getMessage());
            throw new RuntimeException("Error processing request", e);
          }
        }).thenAccept(result -> {
          if (result != null) {
            futureMap.get(request.getUserPointDTO().id().get()).complete((UserPointDTO) result);
          }
        });
  }
  AtomicLong chargePoint = new AtomicLong(0);
  private final ConcurrentHashMap<Long, AtomicLong> userChargePoints = new ConcurrentHashMap<>();
  @Async
  public CompletableFuture<Void> processCharge(UserPointDTO userPointDTO) {
    return CompletableFuture.runAsync(() -> {
      try {
        UserPoint userPoint = UserPointDTO.convertToEntity(userPointDTO);
        validatePositiveAmount(userPoint);

        userChargePoints.computeIfAbsent(userPointDTO.id().get(), k -> new AtomicLong(0))
            .addAndGet(userPoint.point());

        System.out.println(userChargePoints.get(userPointDTO.id()).get() + " 충전중");

        long currentAmount = getCurrentChargeAmount(userPointDTO);
        validateCurrentAmount(currentAmount);
        processUpdatingUserPointAndHistory(userPoint, currentAmount, TransactionType.CHARGE);
      } catch (Exception e) {
        log.error("Error processing charge for user {}: {}", userPointDTO.id(), e.getMessage());
        throw new RuntimeException("Error processing charge", e);
      }
    });
  }

  private long getCurrentChargeAmount(UserPointDTO userPointDTO) {
    AtomicLong userChargePoint = userChargePoints.get(userPointDTO.id());
    return userChargePoint != null ? userChargePoint.get() : 0;
  }
  private UserPointDTO processUpdatingUserPointAndHistory(UserPoint userPoint, long currentAmount,
      TransactionType charge) {
    UserPoint outputChargePoint = updateUserPointAndHistory(userPoint,
        currentAmount, charge);
    return UserPointDTO.convertToDTO(outputChargePoint);
  }

  private UserPoint updateUserPointAndHistory(UserPoint userPoint, long currentAmount,
      TransactionType charge) {
    UserPoint outputPoint = pointRepository.insertOrUpdate(userPoint.id(), currentAmount);
    PointHistory pointHistory = pointRepository.insertHistory(userPoint.id(), userPoint.point(),
        charge,
        Instant.now().toEpochMilli());
    if (pointHistory == null) {
      log.error("History not found");
      throw new IllegalArgumentException("History not found");
    }
    return outputPoint;
  }

  private static void validateCurrentAmount(long currentAmount) {
    if (currentAmount == Long.MIN_VALUE) {
      log.error("Amount exceeds Long.MAX_VALUE");
      throw new IllegalArgumentException("Amount exceeds Long.MAX_VALUE");
    }
  }

  private long getCurrentChargeAmount(UserPointDTO userPointDTO, AtomicLong chargePoint) {
    return pointRepository
        .getById(chargePoint.get())
        .point() + userPointDTO.point().get();
  }

  private static void validatePositiveAmount(UserPoint userPoint) {
    validatePositiveAmount(userPoint.point(), "Amount must be positive");
  }

  public UserPointDTO processUse(UserPointDTO userPointDTO) {
    UserPoint userPoint = UserPointDTO.convertToEntity(userPointDTO);
    validatePositiveAmount(userPoint);
    long currentAmount = getCurrentUseAmount(userPointDTO, userPoint);
    validatePositiveAmount(currentAmount, "Amount exceeds balance");
    return processUpdatingUserPointAndHistory(userPoint, currentAmount, TransactionType.USE);
  }

  private long getCurrentUseAmount(UserPointDTO userPointDTO, UserPoint userPoint) {
    return pointRepository
        .getById(userPoint.id())
        .point() - userPointDTO.point().get();
  }

  private static void validatePositiveAmount(long currentAmount, String Amount_exceeds_balance) {
    if (currentAmount < 0) {
      throw new IllegalArgumentException(Amount_exceeds_balance);
    }
  }

  /**
   * 비동기 처리를 위한 Future를 완료
   *
   * @param id : UserPointDTO의 id
   */
  private void completeFuture(long id) {
    UserPoint byId = pointRepository.getById(id);
    CompletableFuture<UserPointDTO> future = futureMap.get(id);
    if (byId != null && future != null) {
      future.complete(UserPointDTO.convertToDTO(byId));
    }

  }
}