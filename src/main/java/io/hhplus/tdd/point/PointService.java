package io.hhplus.tdd.point;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PointService {

  private final PointRepository pointRepository;
  //TimeStamp를 기준으로 우선순위를 정하는 우선순위 큐
  private final Queue<QueueEntity> requestQueue = new PriorityQueue<>();
  // 동시성 해결을 위한 원자성보장
  private final ConcurrentHashMap<Long, AtomicLong> userPoints = new ConcurrentHashMap<>();

  public PointService(PointRepository pointRepository) {
    this.pointRepository = pointRepository;
  }

  public void addToQueueByCharge(long id, long amount) {
    requestQueue.offer(new QueueEntity(id, amount, TransactionType.CHARGE));

  }

  public void addToQueueByUse(long id, long amount) {
    requestQueue.offer(new QueueEntity(id, amount, TransactionType.USE));
  }


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
      }
    }

  }

  public UserPoint point(long id) {
    if (id < 0) {
      throw new IllegalArgumentException("id must be positive");
    }
    return pointRepository.selectById(id);
  }

  public List<PointHistory> history(long id) {
    return pointRepository.selectHistories(id);
  }

  UserPoint charge(long id, long amount) {
    addToQueueByCharge(id, amount);
    return pointRepository.selectById(id);
  }

  private void chargeProcess(long id, long amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("amount must be positive");
    }
    userPoints.putIfAbsent(id, new AtomicLong(0));
    long currentAmount = userPoints.get(id).addAndGet(amount);
    if (currentAmount == Long.MIN_VALUE) {
      throw new IllegalArgumentException("amount is exceed Long.MAX_VALUE");
    }
    pointRepository.insertOrUpdate(id, currentAmount);
    pointRepository.insertHistory(id, amount, TransactionType.CHARGE, System.currentTimeMillis());
  }

  public UserPoint use(long id, long amount) {
    addToQueueByUse(id, amount);
    return pointRepository.selectById(id);
  }

  private void useProcess(long id, long amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("amount must be positive");
    }
    userPoints.putIfAbsent(id, new AtomicLong(0));
    long currentAmount = userPoints.get(id).addAndGet(-amount);
    if (currentAmount < 0) {
      throw new IllegalArgumentException("amount is more than balance");
    }
    pointRepository.insertOrUpdate(id, currentAmount);
    pointRepository.insertHistory(id, amount, TransactionType.USE, System.currentTimeMillis());
  }
}
