package io.hhplus.tdd.point;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
/**
 * 해결해야하는 동시성
 * 1. 레이스컨디션
 * 2. 데드락
 * 3. 로스트 업데이트
 * 4. 논 리피터블 리드
 * 5. 팬텀 읽기
 */
public class PointServiceConcurrencyTest {
  private final PointService pointService = new PointService(new PointRepositoryImpl());



  // 레이스컨디션
  @Test
  public void testRaceCondition() throws InterruptedException {
    //given
    long userId = 1L;
    long initialAmount = 100L;
    pointService.charge(userId, initialAmount);
    int threadCount = 10;
    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);
    //when
    for (int i = 0; i < threadCount; i++) {
      executorService.execute(() -> {
        pointService.addToQueueByCharge(userId, 10L);
        latch.countDown();
      });
    }
    latch.await();
    pointService.queueOperation();
    UserPoint userPoint = pointService.point(userId);
    //then
    assertEquals(initialAmount + 10 * threadCount, userPoint.point());
  }
  // 데드락
  @Test
  public void testDeadlock() throws InterruptedException {
    //given
    long userId1 = 1L;
    long userId2 = 2L;
    pointService.addToQueueByCharge(userId1, 100L);
    pointService.addToQueueByCharge(userId2, 100L);
    ExecutorService executorService = Executors.newFixedThreadPool(2);
    CountDownLatch latch = new CountDownLatch(2);
    //when
    executorService.execute(() -> {
      pointService.addToQueueByCharge(userId1, 10L);
      pointService.addToQueueByUse(userId2, 10L);
      latch.countDown();
    });

    executorService.execute(() -> {
      pointService.addToQueueByCharge(userId2, 10L);
      pointService.addToQueueByUse(userId1, 10L);
      latch.countDown();
    });
    latch.await();
    //then
    pointService.queueOperation();
    UserPoint userPoint1 = pointService.point(userId1);
    UserPoint userPoint2 = pointService.point(userId2);
    pointService.history(userId1).forEach(System.out::println);
    pointService.history(userId2).forEach(System.out::println);
    assertEquals(userPoint1.point(), 100);
    assertEquals(userPoint2.point(), 100);
  }
  // 로스트 업데이트
  @Test
  public void testLostUpdate() throws InterruptedException {
    long userId = 1L;
    pointService.charge(userId, 100L);

    ExecutorService executorService = Executors.newFixedThreadPool(2);
    CountDownLatch latch = new CountDownLatch(2);

    executorService.execute(() -> {
      pointService.charge(userId, 50L);
      latch.countDown();
    });

    //Lost update
    executorService.execute(() -> {
      pointService.use(userId, 30L);
      latch.countDown();
    });

    latch.await();
    UserPoint userPoint = pointService.point(userId);
    System.out.println("Final amount: " + userPoint.point());
  }
  // 논 리피터블 리드
  @Test
  public void testNonRepeatableRead() throws InterruptedException {
    long userId = 1L;
    pointService.charge(userId, 100L);

    ExecutorService executorService = Executors.newFixedThreadPool(2);
    CountDownLatch latch = new CountDownLatch(2);
    executorService.execute(() -> {
      UserPoint userPoint = pointService.point(userId);
      System.out.println("First read: " + userPoint.point());
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      //Non -repeatable read : 같은 읽기를 반복할수 없는 경우
      UserPoint secondUserPoint = pointService.point(userId);
      System.out.println("Second read: " + secondUserPoint.point());
      latch.countDown();
    });

    executorService.execute(() -> {
      pointService.addToQueueByCharge(userId, 50L);
      latch.countDown();
    });

    latch.await();
    pointService.queueOperation();
  }

  // 팬텀 읽기
  @Test
  public void testPhantomRead() throws InterruptedException {
    long userId = 1L;
    pointService.charge(userId, 100L);

    ExecutorService executorService = Executors.newFixedThreadPool(2);
    CountDownLatch latch = new CountDownLatch(2);

    executorService.execute(() -> {
      UserPoint userPoint = pointService.point(userId);
      System.out.println("First read: " + userPoint.point());
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      //Phantom read : 같은 쿼리를 반복할때 결과가 달라지는 경우
      UserPoint secondUserPoint = pointService.point(userId);
      System.out.println("Second read: " +secondUserPoint.point());
      latch.countDown();
    });

    executorService.execute(() -> {
      pointService.addToQueueByCharge(userId, 50L);
      latch.countDown();
    });

    pointService.queueOperation();
    latch.await();
  }



}
