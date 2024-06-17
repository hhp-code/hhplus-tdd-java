package io.hhplus.tdd.point;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

public class PointServiceConcurrencyTest {
  private final PointService pointService = new PointService(new UserPointTable(), new PointHistoryTable());
  /**
   * 10개의 스레드를 통해 10원을 10번 충전하고, 마지막에 100원을 사용하며 정확하게 0원이 되는지 확인합니다.
   */
  @Test
  void charge_concurrency() throws InterruptedException {
    // given
    long id = 1;
    int numberOfThreads = 10;
    long chargeAmount = 10;
    ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
    CountDownLatch latch = new CountDownLatch(numberOfThreads);

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
    UserPoint finalPoint = pointService.use(id, 100);
    assertEquals(0, finalPoint.point());
  }


  /**
   * 10개의 스레드를 통해 100원을 10번 사용하도록 요청하고, 최종 잔액이 정확하게 0원이 되는지 확인합니다.
   * @throws InterruptedException
   */
  @Test
  void use_concurrency() throws InterruptedException {
    // given
    long id = 1;
    long initialAmount = 100;
    pointService.charge(id, initialAmount);

    int numberOfThreads = 10;
    long useAmount = 10;
    ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
    CountDownLatch latch = new CountDownLatch(numberOfThreads);
    AtomicInteger successCount = new AtomicInteger();
    AtomicInteger failCount = new AtomicInteger();

    // when
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

    assertDoesNotThrow(() -> {
      latch.await();
    });

    UserPoint finalUserPoint = pointService.point(id);
    long expectedFinalAmount = initialAmount - (successCount.get() * useAmount);
    assertEquals(expectedFinalAmount, finalUserPoint.point());
  }
}
