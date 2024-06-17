package io.hhplus.tdd.point;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PointService {

  private final PointRepository pointRepository;
  private final Queue<QueueEntity> requestQueue = new ConcurrentLinkedQueue<>();


  public PointService(PointRepository pointRepository) {
    this.pointRepository = pointRepository;
  }

  public void addToQueueByCharge(long id, long amount) {
    requestQueue.offer(new QueueEntity(id, amount, TransactionType.CHARGE));
  }

  public void addToQueueByUse(long id, long amount) {
    requestQueue.offer(new QueueEntity(id, amount, TransactionType.USE));
  }

  public UserPoint queueOperation(long id) {
    while (!requestQueue.isEmpty()) {
      QueueEntity requestBuilder = requestQueue.poll();
      if (requestBuilder != null) {
        if (requestBuilder.transactionType == TransactionType.CHARGE) {
          chargeProcess(requestBuilder.id, requestBuilder.amount);
        } else if (requestBuilder.transactionType == TransactionType.USE) {
          useProcess(requestBuilder.id, requestBuilder.amount);
        }
      }
    }
    return pointRepository.selectById(id);
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
    chargeProcess(id, amount);
    return pointRepository.selectById(id);
  }

  private void chargeProcess(long id, long amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("amount must be positive");
    }
    UserPoint userPoint;
    userPoint = pointRepository.selectById(id);
    if (userPoint.point() + amount == Long.MIN_VALUE) {
      throw new IllegalArgumentException("amount is exceed Long.MAX_VALUE");
    }
    amount += userPoint.point();
    pointRepository.insertOrUpdate(id, amount);
    pointRepository.insertHistory(id, amount, TransactionType.CHARGE, System.currentTimeMillis());
  }

  public UserPoint use(long id, long amount) {
    useProcess(id, amount);
    return pointRepository.selectById(id);
  }

  private void useProcess(long id, long amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("amount must be positive");
    }
    UserPoint userPoint = pointRepository.selectById(id);
    if (userPoint.point() - amount < 0) {
      throw new IllegalArgumentException("amount is more than balance");
    }
    long remaining = userPoint.point() - amount;
    pointRepository.insertOrUpdate(id, remaining);
    pointRepository.insertHistory(id, amount, TransactionType.USE, System.currentTimeMillis());
  }
}
