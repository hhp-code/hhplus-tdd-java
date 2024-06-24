package io.hhplus.tdd.point.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.UserPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.dto.UserPointDTO;
import io.hhplus.tdd.point.repository.PointRepository;
import io.hhplus.tdd.point.repository.PointRepositoryImpl;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 해결해야하는 동시성 1. 레이스컨디션 : 두개 이상의 스레드가 동시에 같은 데이터를 변경할때 예상되는 결과값이 나오지않는 문제 2. 데드락 : 두개 이상의 스레드가 서로의
 * 락을 기다리는 상황 3. 로스트 업데이트 : 두개 이상의 스레드가 동시에 같은 데이터를 변경할때 업데이트값이 조회되지않는 문제 4. 논 리피터블 리드 : 같은 쿼리를 반복할때
 * 업데이트 쿼리로 인하여 결과가 달라지는 문제 5. 팬텀 읽기 : 같은 쿼리를 반복했으나 예상되지않은 값을 읽어오는 문제
 */
@SpringBootTest
public class QueueConcurrencyServiceTest {

  static {
    LoggerFactory.getLogger(QueueConcurrencyServiceTest.class);
  }

  private final PointRepository pointRepository = new PointRepositoryImpl(new PointHistoryTable(),
      new UserPointTable());
  private final PointImpl pointImpl = new PointImpl(pointRepository);
  private final HistoryImpl historyImpl = new HistoryImpl(pointRepository);
  private final QueueManager queueManager = new QueueManager(pointRepository);
  private final UseImpl useImpl = new UseImpl(queueManager);
  private final ChargeImpl chargeImpl = new ChargeImpl(queueManager);
  private final PointService pointService = new PointService(pointImpl, historyImpl, useImpl,
      chargeImpl);

  // 레이스컨디션에 대한 테스트
  @Test
  public void testRaceCondition() {
    // given
    long userId = 1L;
    long initialAmount = 100L;
    long plusAmount = 10L;
    UserPointDTO userPointDTO = new UserPointDTO(userId, initialAmount);
    UserPointDTO plusUserPointDTO = new UserPointDTO(userId, plusAmount);
    queueManager.addToQueue(userPointDTO, TransactionType.CHARGE);
    queueManager.processQueue();

    AtomicLong beforeAmount = new AtomicLong(0);
    // when
   CompletableFuture.allOf(
        CompletableFuture.runAsync(
            () -> {
              for (int i = 0; i < 4; i++) {
                queueManager.addToQueue(plusUserPointDTO, TransactionType.CHARGE);
                beforeAmount.addAndGet(1);
              }
            }),
        CompletableFuture.runAsync(
            () -> {
              for (int i = 0; i < 4; i++) {
                queueManager.addToQueue(plusUserPointDTO, TransactionType.CHARGE);
                beforeAmount.addAndGet(1);
              }
            })).join();
    System.out.println("queueManager.getQueue().size() = " + queueManager.getRequestQueue().size());
    queueManager.processQueue();
    UserPoint byId = pointRepository.getById(userId);
    System.out.println("byId = " + byId + "byIdAmount" + byId.point());
    System.out.println("beforeAmount.get() = " + beforeAmount.get());

  }

  // 데드락
  @Test
  public void testDeadlock() throws InterruptedException {
    // given
    long userId1 = 1L;
    long userId2 = 2L;
    long transactionAmount = 100L;
    long deadLockAmount = 10L;
    UserPointDTO userPointDTO1 = new UserPointDTO(userId1, transactionAmount);
    UserPointDTO userPointDTO2 = new UserPointDTO(userId2, transactionAmount);
    queueManager.addToQueue(userPointDTO1, TransactionType.CHARGE);
    queueManager.addToQueue(userPointDTO2, TransactionType.CHARGE);

    queueManager.processQueue();
    // when
    // 스레드 1은 userId1을 충전하고 userId2를 사용
    CompletableFuture.allOf(
        CompletableFuture.runAsync(
            () -> {
              queueManager.addToQueue(new UserPointDTO(userId1, deadLockAmount),
                  TransactionType.CHARGE);
              queueManager.addToQueue(new UserPointDTO(userId2, deadLockAmount),
                  TransactionType.USE);
            }),
        CompletableFuture.runAsync(
            () -> {
              queueManager.addToQueue(new UserPointDTO(userId2, deadLockAmount),
                  TransactionType.CHARGE);
              queueManager.addToQueue(new UserPointDTO(userId1, deadLockAmount),
                  TransactionType.USE);
            })).join();
    // 큐에 있는 작업을 처리
    queueManager.processQueue();
    UserPointDTO userPoint1 = pointService.point(userId1);
    UserPointDTO userPoint2 = pointService.point(userId2);
    // then
    assertEquals(userPoint1.point(), 100);
    assertEquals(userPoint2.point(), 100);
  }

  // 로스트 업데이트
  @Test
  public void testLostUpdate() {
    // given
    long userId = 1L;
    long point = 100L;
    pointRepository.insertOrUpdate(userId, point);
    long chargePoint = 50L;
    long usePoint = 30L;

    // when
    // 스레드 1은 userId를 50충전
    CompletableFuture.allOf(
        CompletableFuture.runAsync(
            () -> {
              queueManager.addToQueue(new UserPointDTO(userId, chargePoint),
                  TransactionType.CHARGE);
            }),
        CompletableFuture.runAsync(
            () -> {
              queueManager.addToQueue(new UserPointDTO(userId, usePoint), TransactionType.USE);
            })).join();

    // 큐에 있는 작업을 처리
    queueManager.processQueue();
    UserPointDTO userPoint = pointService.point(userId);
    // then
    assertEquals(point + chargePoint - usePoint, userPoint.point());
  }

  // 논 리피터블 리드
  @Test
  public void testNonRepeatableRead() throws InterruptedException {
    // given
    long userId = 1L;
    long point = 100L;
    pointRepository.insertOrUpdate(userId, point);

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
          assertEquals(userPoint.point(), secondUserPoint.point());
          latch.countDown();
        });

    executorService.execute(
        () -> {
          queueManager.addToQueue(new UserPointDTO(userId, 50L), TransactionType.CHARGE);
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
    pointRepository.insertOrUpdate(userId, 100L);
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
          beforeAmount.addAndGet(userPoint.point().get());
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          // Phantom read : 같은 쿼리를 반복할때 결과가 달라지는 경우
          UserPointDTO secondUserPoint = pointService.point(userId);
          afterAmount.addAndGet(secondUserPoint.point().get());
          latch.countDown();
        });

    executorService.execute(
        () -> {
          queueManager.addToQueue(new UserPointDTO(userId, 50L), TransactionType.CHARGE);
          latch.countDown();
        });
    latch.await();
    // 큐에 있는 작업을 처리
    queueManager.processQueue();
    // then
    assertEquals(beforeAmount.get(), afterAmount.get());
  }
}
