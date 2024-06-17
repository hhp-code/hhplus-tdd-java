package io.hhplus.tdd.point;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

public class PointServiceSynchronizedTest {

  private final PointService pointService =
      new PointService(new PointRepositoryImpl()) {
        @Override
        public synchronized UserPoint charge(long id, long amount) {
          return super.charge(id, amount);
        }

        @Override
        public synchronized UserPoint use(long id, long amount) {
          return super.use(id, amount);
        }
      };

  /**
   * 10개의 스레드를 통해 10원을 100번 충전하고, 마지막에 1000원을 사용하며 정확하게 0원이 되는지 확인합니다.
   * synchronized : 51112ms
   */
  @Test
  void charge_concurrency() throws InterruptedException {
    // given
    long id = 1;
    int numberOfThreads = 100;
    long chargeAmount = 10;
    ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
    CountDownLatch latch = new CountDownLatch(numberOfThreads);
    long startTime = System.currentTimeMillis();

    // when
    for (int i = 0; i < numberOfThreads; i++) {
      executorService.submit(() -> {
        pointService.charge(id, chargeAmount);
        latch.countDown();
      });
    }

    // then
    latch.await();
    executorService.shutdown();
    UserPoint finalPoint = pointService.use(id, 1000);
    long endTime = System.currentTimeMillis();
    System.out.println( endTime - startTime +"ms");
    assertEquals(0, finalPoint.point());
  }


  /**
   * 100개의 스레드를 통해 10원을 100번 사용하도록 요청하고, 최종 잔액이 정확하게 0원이 되는지 확인합니다.
   * synchronized : 53564ms
   */
  @Test
  void use_concurrency() throws InterruptedException {
    // given
    long id = 1;
    long initialAmount = 1000;

    int numberOfThreads = 100;
    long useAmount = 10;
    ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
    CountDownLatch latch = new CountDownLatch(numberOfThreads);
    AtomicInteger successCount = new AtomicInteger();
    AtomicInteger failCount = new AtomicInteger();
    long startTime = System.currentTimeMillis();
    // when
    pointService.charge(id, initialAmount);
    for (int i = 0; i < numberOfThreads; i++) {
      executorService.submit(() -> {
        try {
          pointService.use(id, useAmount);
          successCount.incrementAndGet();
        } catch (Exception e) {
          failCount.incrementAndGet();
        } finally {
          latch.countDown();
        }
      });
    }
    // then
    latch.await();
    executorService.shutdown();

    assertDoesNotThrow(() -> latch.await());
    UserPoint finalUserPoint = pointService.point(id);
    long expectedFinalAmount = initialAmount - (successCount.get() * useAmount);
    long endTime = System.currentTimeMillis();
    System.out.println(endTime - startTime + "ms");
    assertEquals(expectedFinalAmount, finalUserPoint.point());
  }
}
