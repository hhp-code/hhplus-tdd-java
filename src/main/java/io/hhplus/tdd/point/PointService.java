package io.hhplus.tdd.point;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 주요 로직 1. 포인트 충전 또는 사용 요청을 큐에 추가, CompleatableFuture를 결과값을 비동기로 대기합니다. 2. 큐 객체가 생성되면서 큐 객체에는
 * 타임스탬프가 존재하여 큐에 추가된 순서대로 처리됨. 3. 큐에서 나온요청은 AtomicLong을 저장하는 ConcurrentHashMap에 의해 값 비교를 하며 갱신됩니다.
 * 4. 주기적으로 큐에 있는 요청을 처리하면서 포인트를 갱신합니다. 그리고 결과값을 CompletableFuture에 저장합니다. 5. 처음에 요청했던 값을
 * CompletableFuture를 통해 비동기로 반환합니다.
 *
 * <p>의문 사항 1. CompleatableFuture를 사용하여 결과값을 기다리지만, 결과값이 없을경우에 대한 로직이 빈약한것같습니다. 예상 개선 사항 1. 만약에 비동기
 * 처리된 결과값이 반환되지않을 경우 대기시간을 설정해야 할수도 있을것 같습니다.
 */
@Slf4j
@Service
public class PointService {

  private final PointRepository pointRepository;
  // 요청을 처리하기 위한 ConcurrentLinkedQueue
  private final Queue<QueueEntity> requestQueue = new ConcurrentLinkedQueue<>();
  // 포인트 갱신을 위한 ConcurrentHashMap
  private final Map<Long, AtomicLong> userPoints = new ConcurrentHashMap<>();
  // 비동기 처리를 위한 CompletableFuture
  final Map<Long, CompletableFuture<UserPointDTO>> futureMap = new ConcurrentHashMap<>();
  // 비동기 대기를 위한 타임아웃 설정
  private static final Duration TIMEOUT = Duration.ofSeconds(5);

  public PointService(PointRepository pointRepository) {
    this.pointRepository = pointRepository;
  }

  // 포인트 충전 요청을 큐에 추가
  public CompletableFuture<UserPointDTO> addToQueueByCharge(long id, long amount) {
    CompletableFuture<UserPointDTO> future = new CompletableFuture<>();
    requestQueue.offer(new QueueEntity(id, amount, TransactionType.CHARGE));
    futureMap.put(id, future);
    return future;
  }

  // 포인트 사용 요청을 큐에 추가
  public CompletableFuture<UserPointDTO> addToQueueByUse(long id, long amount) {
    CompletableFuture<UserPointDTO> future = new CompletableFuture<>();
    requestQueue.offer(new QueueEntity(id, amount, TransactionType.USE));
    futureMap.put(id, future);
    return future;
  }

  // 큐에 있는 요청을 주기적으로 처리
  @Scheduled(fixedDelay = 1000)
  public void queueOperation() {
    while (!requestQueue.isEmpty()) {
      QueueEntity request = requestQueue.poll();
      if (request != null) {
        if (request.transactionType == TransactionType.CHARGE) {
          chargeProcess(request.id, request.amount);
        } else if (request.transactionType == TransactionType.USE) {
          useProcess(request.id, request.amount);
        }
        Optional<UserPointDTO> result = pointRepository.selectById(request.id);
        if (result.isPresent()) {
          futureMap.get(request.id).complete(result.get());
        } else {
          futureMap
              .get(request.id)
              .completeExceptionally(new IllegalArgumentException("UserPointDTO is not found"));
        }
      }
    }
  }

  // CompletableFuture를 통해 비동기 처리된 결과를 반환
  public UserPointDTO futureMapListener(long id) {
    try {
      return futureMap.get(id).get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      Thread.currentThread().interrupt();
      errorMessageThrowing("처리요청이 완료되지 않았습니다.");
    }
    return null;
  }

  // 포인트 조회
  public UserPointDTO point(long id) {
    if (id < 0) {
      throw new IllegalArgumentException("getId must be positive");
    }
    return pointRepository
        .selectById(id)
        .orElseThrow(
            () -> {
              log.error("server error");
              return new IllegalArgumentException("server error");
            });
  }

  public List<PointHistoryDTO> history(long id) {
    return pointRepository
        .selectHistories(id)
        .orElseThrow(
            () -> {
              log.error("server error");
              return new IllegalArgumentException("server error");
            });
  }

  // 컨트롤러 단에서 받아온 충전 요청을 큐에 추가 및 비동기 결과값 대기
  public UserPointDTO charge(long id, long amount) {
    try {
      addToQueueByCharge(id, amount).get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
    } catch (ExecutionException | InterruptedException e) {
      Thread.currentThread().interrupt();
      errorMessageThrowing("서버 에러가 발생했습니다.");
    } catch (TimeoutException e) {
      errorMessageThrowing("처리 시간이 초과되었습니다.");
    }
    return null;
  }

  // 포인트 충전 처리
  private void chargeProcess(long id, long amount) {
    if (amount < 0) {
      errorMessageThrowing("getAmount must be positive");
    }
    userPoints.putIfAbsent(id, new AtomicLong(0));
    long currentAmount = userPoints.get(id).addAndGet(amount);
    if (currentAmount == Long.MIN_VALUE) {
      errorMessageThrowing("getAmount is exceed Long.MAX_VALUE");
    }
    pointRepository.insertOrUpdate(id, currentAmount);
    pointRepository.insertHistory(id, amount, TransactionType.CHARGE, System.currentTimeMillis());
  }

  // 컨트롤러 단에서 받아온 포인트 사용 요청을 큐에 추가 및 비동기 결과값 대기

  public UserPointDTO use(long id, long amount) {
    try {
      return addToQueueByUse(id, amount).get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
    } catch (InterruptedException | ExecutionException e) {
      Thread.currentThread().interrupt();
      errorMessageThrowing("처리 중 에러가 발생했습니다.");
    } catch (TimeoutException e) {
        errorMessageThrowing("처리 시간이 초과되었습니다.");
    }
    return null;
  }

  // 포인트 사용 처리
  private void useProcess(long id, long amount) {
    if (amount < 0) {
      errorMessageThrowing("getAmount must be positive");
    }
    userPoints.putIfAbsent(id, new AtomicLong(0));
    long currentAmount = userPoints.get(id).addAndGet(-amount);
    if (currentAmount < 0) {
      errorMessageThrowing("getAmount is more than balance");
    }
    pointRepository.insertOrUpdate(id, currentAmount);
    pointRepository.insertHistory(id, amount, TransactionType.USE, System.currentTimeMillis());
  }

  // 에러 메시지 출력 및 예외 발생
  private static void errorMessageThrowing(String message) {
    log.error(message);
    throw new IllegalArgumentException(message);
  }
}
