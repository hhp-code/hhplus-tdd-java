package io.hhplus.tdd.point;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;

public class PointServiceQueueTest {
  private final PointService pointService = new PointService(new PointRepositoryImpl());

  /**
   *  10개의 스레드를 통해 10원을 100번 충전하고, 5원을 100번 사용하며 정확하게 500원이 되는지 확인합니다.
   * synchronized : 51112ms
   */
  @Test
  void charge_and_use_concurrency() throws InterruptedException {
    // given
    long id = 1;
    int numberOfThreads = 100;
    long chargeAmount = 10;
    ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
    CountDownLatch latch = new CountDownLatch(numberOfThreads);
    long startTime = System.currentTimeMillis();

    // when
    for (int i = 0; i < numberOfThreads; i++) {
      executorService.submit(
          () -> {
            pointService.addToQueueByCharge(id, chargeAmount);
            pointService.addToQueueByUse(id,5);
            latch.countDown();
          });
    }

    // then
    latch.await();
    executorService.shutdown();
    UserPoint operation = pointService.queueOperation(id);
    System.out.println(operation.point());
    long endTime = System.currentTimeMillis();
    System.out.println(endTime - startTime + "ms");
    assertEquals(500, operation.point());
  }



}
