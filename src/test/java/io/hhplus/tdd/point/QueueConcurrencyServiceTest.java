package io.hhplus.tdd.point;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import io.hhplus.tdd.point.repository.PointRepository;
import io.hhplus.tdd.point.repository.PointRepositoryImpl;
import io.hhplus.tdd.point.service.charge.ChargeImpl;
import io.hhplus.tdd.point.service.PointService;
import io.hhplus.tdd.point.service.QueueManager;
import io.hhplus.tdd.point.service.history.HistoryImpl;
import io.hhplus.tdd.point.service.point.PointImpl;
import io.hhplus.tdd.point.service.use.UseImpl;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

/**
 * 해결해야하는 동시성 1. 레이스컨디션 : 두개 이상의 스레드가 동시에 같은 데이터를 변경할때 예상되는 결과값이 나오지않는 문제 2. 데드락 : 두개 이상의 스레드가 서로의
 * 락을 기다리는 상황 3. 로스트 업데이트 : 두개 이상의 스레드가 동시에 같은 데이터를 변경할때 업데이트값이 조회되지않는 문제 4. 논 리피터블 리드 : 같은 쿼리를 반복할때
 * 업데이트 쿼리로 인하여 결과가 달라지는 문제 5. 팬텀 읽기 : 같은 쿼리를 반복했으나 예상되지않은 값을 읽어오는 문제
 */
public class QueueConcurrencyServiceTest {
  static {
    LoggerFactory.getLogger(QueueConcurrencyServiceTest.class);
  }

  private final PointRepository pointRepository = new PointRepositoryImpl();
  private final UseImpl useImpl = new UseImpl(pointRepository);
  private final PointImpl pointImpl = new PointImpl(pointRepository);
  private final ChargeImpl chargeImpl = new ChargeImpl(pointRepository);
  private final HistoryImpl historyImpl = new HistoryImpl(pointRepository);
  private final QueueManager queueManager = new QueueManager(pointRepository, chargeImpl, useImpl);
  private final PointService pointService = new PointService(queueManager, pointImpl, historyImpl);

  // 레이스컨디션
  @Test
  public void testRaceCondition() throws InterruptedException {
    // given
    long userId = 1L;
    long initialAmount = 100L;
    queueManager.addToQueue(userId, initialAmount, TransactionType.CHARGE);
    queueManager.processQueue();
    int threadCount = 10;
    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);
    // when
    // 10개의 스레드가 동시에 10씩 충전
    for (int i = 0; i < threadCount; i++) {
      executorService.execute(
          () -> {
            queueManager.addToQueue(userId, 10L, TransactionType.CHARGE);
            latch.countDown();
          });
    }
    latch.await();
    // 큐에 있는 작업을 처리
    queueManager.processQueue();
    UserPointDTO userPoint = pointService.point(userId);
    // then
    assertEquals(initialAmount + 10 * threadCount, userPoint.getPoint());
  }

  // 데드락
  @Test
  public void testDeadlock() throws InterruptedException {
    // given
    long userId1 = 1L;
    long userId2 = 2L;
    queueManager.addToQueue(userId1, 100L, TransactionType.CHARGE);
    queueManager.addToQueue(userId2, 100L, TransactionType.CHARGE);

    queueManager.processQueue();
    ExecutorService executorService = Executors.newFixedThreadPool(2);
    CountDownLatch latch = new CountDownLatch(2);
    // when
    // 스레드 1은 userId1을 충전하고 userId2를 사용
    executorService.execute(
        () -> {
          queueManager.addToQueue(userId1, 10L, TransactionType.CHARGE);
          queueManager.addToQueue(userId2, 10L, TransactionType.USE);
          latch.countDown();
        });

    // 스레드 2는 userId2를 충전하고 userId1을 사용
    executorService.execute(
        () -> {
          queueManager.addToQueue(userId2, 10L, TransactionType.CHARGE);
          queueManager.addToQueue(userId1, 10L, TransactionType.USE);
          latch.countDown();
        });
    latch.await();
    // 큐에 있는 작업을 처리
    queueManager.processQueue();
    UserPointDTO userPoint1 = pointService.point(userId1);
    UserPointDTO userPoint2 = pointService.point(userId2);
    // then
    assertEquals(userPoint1.getPoint(), 100);
    assertEquals(userPoint2.getPoint(), 100);
  }

  // 로스트 업데이트
  @Test
  public void testLostUpdate() throws InterruptedException {
    // given
    long userId = 1L;
    queueManager.addToQueue(userId, 100L, TransactionType.CHARGE);
    queueManager.processQueue();

    ExecutorService executorService = Executors.newFixedThreadPool(2);
    CountDownLatch latch = new CountDownLatch(2);

    // when
    // 스레드 1은 userId를 50충전
    executorService.execute(
        () -> {
          queueManager.addToQueue(userId, 50L, TransactionType.CHARGE);
          latch.countDown();
        });

    // Lost update
    // 스레드 2는 userId를 30충전
    executorService.execute(
        () -> {
          queueManager.addToQueue(userId, 30L, TransactionType.USE);
          latch.countDown();
        });

    latch.await();
    // 큐에 있는 작업을 처리
    queueManager.processQueue();
    UserPointDTO userPoint = pointService.point(userId);
    // then
    assertEquals(120, userPoint.getPoint());
  }

  // 논 리피터블 리드
  @Test
  public void testNonRepeatableRead() throws InterruptedException {
    // given
    long userId = 1L;
    queueManager.addToQueue(userId, 100L, TransactionType.CHARGE);
    queueManager.processQueue();

    ExecutorService executorService = Executors.newFixedThreadPool(2);
    CountDownLatch latch = new CountDownLatch(2);
    // when
    // 첫번째 스레드는 userId를 조회하고 1초후 다시 조회
    executorService.execute(
        () -> {
          UserPointDTO userPoint = pointService.point(userId);
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          // Non -repeatable read : 같은 읽기를 반복할수 없는 경우
          UserPointDTO secondUserPoint = pointService.point(userId);
          // then
          assertEquals(userPoint.getPoint(), secondUserPoint.getPoint());
          latch.countDown();
        });

    executorService.execute(
        () -> {
          queueManager.addToQueue(userId, 50L, TransactionType.CHARGE);
          latch.countDown();
        });

    latch.await();
    // 큐에 있는 작업을 처리
    queueManager.processQueue();
  }

  // 팬텀 읽기
  @Test
  public void testPhantomRead() throws InterruptedException {
    // given
    long userId = 1L;
    queueManager.addToQueue(userId, 100L, TransactionType.CHARGE);
    queueManager.processQueue();
    ExecutorService executorService = Executors.newFixedThreadPool(2);
    CountDownLatch latch = new CountDownLatch(2);

    // AtomicLong을 사용하여 스레드간 변수를 밖으로 빼기
    AtomicLong beforeAmount = new AtomicLong(0);
    AtomicLong afterAmount = new AtomicLong(0);

    // when
    // 첫번째 스레드는 userId를 조회하고 1초후 다시 조회
    executorService.execute(
        () -> {
          UserPointDTO userPoint = pointService.point(userId);
          beforeAmount.addAndGet(userPoint.getPoint());
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          // Phantom read : 같은 쿼리를 반복할때 결과가 달라지는 경우
          UserPointDTO secondUserPoint = pointService.point(userId);
          afterAmount.addAndGet(secondUserPoint.getPoint());
          latch.countDown();
        });

    executorService.execute(
        () -> {
          queueManager.addToQueue(userId, 50L, TransactionType.CHARGE);
          latch.countDown();
        });
    latch.await();
    // 큐에 있는 작업을 처리
    queueManager.processQueue();
    // then
    assertEquals(beforeAmount.get(), afterAmount.get());
  }
}
