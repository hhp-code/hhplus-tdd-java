package io.hhplus.tdd.point.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.dto.UserPointDTO;
import io.hhplus.tdd.point.repository.PointRepository;
import io.hhplus.tdd.point.repository.PointRepositoryImpl;
import io.hhplus.tdd.point.service.charge.ChargeImpl;
import io.hhplus.tdd.point.service.history.HistoryImpl;
import io.hhplus.tdd.point.service.point.PointImpl;
import io.hhplus.tdd.point.service.use.UseImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QueueManagerTest {
  private final PointRepository pointRepository = new PointRepositoryImpl();
  private final ChargeImpl chargeImpl = new ChargeImpl(pointRepository);
  private final UseImpl useImpl = new UseImpl(pointRepository);
  private final PointImpl pointImpl = new PointImpl(pointRepository);
  private final HistoryImpl historyImpl = new HistoryImpl(pointRepository);
  private final QueueManager queueManager = new QueueManager(pointRepository, chargeImpl, useImpl);
  private final PointService pointService = new PointService(queueManager, pointImpl, historyImpl);



  @Test
  @DisplayName("충전 큐 요청이 정상적으로 저장되는지 확인합니다.")
  void charge_and_verify_amounts() {
    // given
    long id = 1;
    long amount = 100;
    // when
    queueManager.addToQueue(new UserPointDTO(id, amount), TransactionType.CHARGE);
    queueManager.processQueue();
    UserPointDTO userPoint = pointService.point(id);
    // then
    assertEquals(userPoint.point(), amount);
  }

  @Test
  @DisplayName("충전 큐에 음수값을 입력하면 예외가 발생합니다.")
  void charge_by_negative_amount_then_throw_exception() {
    // given
    long id = 1;
    long amount = -100;
    // when
      try {
      queueManager.addToQueue(new UserPointDTO(id, amount), TransactionType.CHARGE);
      queueManager.processQueue();
    } catch (IllegalArgumentException e) {
        // then
      assertEquals("Amount must be non-negative", e.getMessage());

    }
  }

  @Test
  @DisplayName("충전 큐에 이전보다 큰 양을 입력하면 충전값이 이전보다 커야합니다.")
  void charge_by_positive_amount_greater_than_before() {
    // given
    long id = 1;
    long amount = 100;
    // when
    queueManager.addToQueue(new UserPointDTO(id, amount), TransactionType.CHARGE);
    queueManager.processQueue();
    UserPointDTO userPoint = pointService.point(id);
    long beforePoint = userPoint.point();
    queueManager.addToQueue(new UserPointDTO(id, amount), TransactionType.CHARGE);
    queueManager.processQueue();
    UserPointDTO secondUserPoint = pointService.point(id);
    long afterPoint = secondUserPoint.point();
    // then
    assertThat(afterPoint).isGreaterThan(beforePoint);
  }

  @Test
  @DisplayName("충전 큐에 Long.MAX_VALUE보다 큰 양을 입력하면 예외가 발생합니다.")
  void charge_by_positive_amount_greater_than_long_max() {
    // given
    long id = 1;
    long amount = Long.MAX_VALUE;
    // when
    queueManager.addToQueue(new UserPointDTO(id, 1L), TransactionType.CHARGE);
    try {
      queueManager.addToQueue(new UserPointDTO(id, amount), TransactionType.CHARGE);
      queueManager.processQueue();
    } catch (IllegalArgumentException e) {
      // then
      assertThat(e.getMessage()).isEqualTo("Amount exceeds Long.MAX_VALUE");
    }
  }

  @Test
  @DisplayName("사용 요청 큐가 정상적으로 저장되는지 확인합니다.")
  void use_after_remain_points() {
    // given
    long id = 1;
    long inputAmount = 100;
    long useAmount = 40;
    long remainAmount = inputAmount - useAmount;
    // when
    queueManager.addToQueue(new UserPointDTO(id, inputAmount), TransactionType.CHARGE);
    queueManager.addToQueue(new UserPointDTO(id, useAmount), TransactionType.USE);
    queueManager.processQueue();
    UserPointDTO userPoint = pointService.point(id);
    // then
    assertEquals(remainAmount, userPoint.point());
  }

  @Test
  @DisplayName("사용 큐에 음수값을 입력하면 예외가 발생합니다.")
  void use_by_negative_amount_then_throw_exception() {
    // given
    long id = 1;
    long amount = -100;
    // when
    try {
      queueManager.addToQueue(new UserPointDTO(id, amount), TransactionType.USE);
      queueManager.processQueue();
    } catch (IllegalArgumentException e) {
      assertEquals("Amount must be non-negative", e.getMessage());
    }
    // then
  }

  @Test
  @DisplayName("사용 큐에 사용량이 잔액보다 크면 예외가 발생합니다.")
  void use_after_negative_points() {
    // given
    long id = 1;
    long inputAmount = 100;
    long useAmount = 200;
    // when
    queueManager.addToQueue(new UserPointDTO(id, inputAmount), TransactionType.CHARGE);
    queueManager.processQueue();
    try {
      queueManager.addToQueue(new UserPointDTO(id, useAmount), TransactionType.USE);
      queueManager.processQueue();
    } catch (IllegalArgumentException e) {
      // then
      assertEquals("Amount exceeds balance", e.getMessage());
    }
  }
}
