package io.hhplus.tdd.point;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;


class PointServiceTest {
  private final PointService pointService = new PointService(new  PointRepositoryImpl());

  /**
   * point 메소드를 일반 입력값을 테스트하고
   * 결과값이 없을때 0이 반환되는지 확인합니다.
   */
  @Test
  void point_by_id_then_no_results(){
    //given
    long id = 2L;
    //when
    UserPointDTO userPoint = pointService.point(id);
    //then
    assertEquals(id, userPoint.getId());
    assertThat(userPoint.getPoint()).isEqualTo(0);
  }
  /**
   * point 메소드를 음수값을 테스트하고, 결과값이 없을때 exception이 발생하는지 확인합니다.
   */
  @Test
  void point_by_negative_id_then_no_results(){
    //given
    long id = -1L;
    //when
    String[] message = new String[1];
    try {
      pointService.point(id);
    } catch (IllegalArgumentException e) {
      message[0] = e.getMessage();
    }
    //then
    assertEquals("id must be positive", message[0]);
  }

  /**
   * charge 메소드에 임시로 충전을 해보고, 충전값이 정상적으로 반영되는지 확인합니다.
   */
  @Test
  void charge_and_verify_amounts(){
    //given
    long id = 1;
    long amount = 100;
    // when
    pointService.addToQueueByCharge(id, amount);
    pointService.queueOperation();
    UserPointDTO userPoint = pointService.point(id);
    // then
    assertEquals(userPoint.getPoint(), amount);
  }
  /**
   * charge 메소드에 음수값을 입력하면 예외가 발생하는지 확인합니다.
   */
  @Test
  void charge_by_negative_amount_then_throw_exception(){
    // given
    long id = 1;
    long amount = -100;
    // when
    String[] message = new String[1];
    try {
      pointService.addToQueueByCharge(id, amount);
      pointService.queueOperation();
    } catch (IllegalArgumentException e) {
      message[0] = e.getMessage();
    }
    // then
    assertEquals("amount must be positive", message[0]);
  }

  /**
   * charge 메소드에 충전을 하고나서 충전값이 이전보다 큰지 확인합니다.
   */
  @Test
  void charge_by_positive_amount_greater_than_before(){
    // given
    long id = 1;
    long amount = 100;
    // when
    pointService.addToQueueByCharge(id, amount);
    pointService.queueOperation();
    UserPointDTO userPoint = pointService.point(id);
    long beforePoint = userPoint.getPoint();
    pointService.addToQueueByCharge(id, amount);
    pointService.queueOperation();
    UserPointDTO secondUserPoint = pointService.point(id);
    long afterPoint = secondUserPoint.getPoint();
    // then
    assertThat(afterPoint).isGreaterThan(beforePoint);
  }

  /**
   * charge 메소드에 충전을 하고나서 충전값이 long 타입의 최대값을 넘어가는지 확인합니다.
   */
  @Test
  void charge_by_positive_amount_greater_than_long_max(){
    // given
    long id = 1;
    long amount = Long.MAX_VALUE;
    // when
    pointService.addToQueueByCharge(id, 1L);
    String[] message = new String[1];
    try{
      pointService.addToQueueByCharge(id, amount);
      pointService.queueOperation();
    }
    catch (IllegalArgumentException e){
      message[0] = e.getMessage();
    }
    // then
    assertThat(message[0]).isEqualTo("amount is exceed Long.MAX_VALUE");
  }


  /**
   * use 메소드에 액수 사용을 하고나서 사용값이 정상적으로 반영되는지 확인합니다.
   */
  @Test
  void use_after_remain_points(){
    //given
    long id = 1;
    long inputAmount = 100;
    long useAmount = 40;
    long remainAmount = inputAmount - useAmount;
    // when
    pointService.addToQueueByCharge(id,inputAmount);
    pointService.addToQueueByUse(id, useAmount);
    pointService.queueOperation();
    UserPointDTO userPoint = pointService.point(id);
    // then
    assertEquals(remainAmount, userPoint.getPoint());
  }
  /**
   * use 메소드에 음수값을 입력하면 예외가 발생하는지 확인합니다.
   */
  @Test
  void use_by_negative_amount_then_throw_exception(){
    // given
    long id = 1;
    long amount = -100;
    // when
    String[] message = new String[1];
    try {
      pointService.addToQueueByUse(id, amount);
      pointService.queueOperation();
    } catch (IllegalArgumentException e) {
      message[0] = e.getMessage();
    }
    // then
    assertEquals("amount must be positive", message[0]);
  }

  /**
   * use 메소드에 사용을 하고나서 값이 음수가 되는지 확인합니다.
   */
  @Test
  void use_after_negative_points(){
    //given
    long id = 1;
    long inputAmount = 100;
    long useAmount = 200;
    // when
    pointService.addToQueueByCharge(id,inputAmount);
    pointService.queueOperation();
    String[] message = new String[1];
    try {
      pointService.addToQueueByUse(id, useAmount);
      pointService.queueOperation();
    } catch (IllegalArgumentException e) {
      message[0] = e.getMessage();
    }
    // then
    assertEquals("amount is more than balance", message[0]);
  }



  /**
   * history 메소드를 테스트하고, 결과값이 없을때 빈 리스트가 반환되는지 확인합니다.
   */
  @Test
  void history_by_id_then_charge_results(){
    // given
    long id = 1;
    // when
    List<PointHistoryDTO> pointHistory = pointService.history(id);
    // then
    assertThat(pointHistory).isEmpty();
  }

  /**
   * history 메소드에서 충전하고나서 충전이력이 출력되는지 확인합니다.
   */
  @Test
  void history_after_charged_then_history_results(){
    // given
    long id = 1;
    long amount = 100;
    // when
    pointService.addToQueueByCharge(id, amount);
    pointService.queueOperation();
    List<PointHistoryDTO> pointHistory = pointService.history(id);
    // then
    pointHistory.forEach(history -> {
      assertThat(history.amount()).isEqualTo(amount);
      assertThat(history.type()).isEqualTo(TransactionType.CHARGE);
    });
  }

  /**
   * history 메소드에서 출금하고나서 출금이력이 출력되는지 확인합니다.
   */
  @Test
  void history_after_use_then_history_results(){
    // given
    long id = 1;
    long amount = 100;
    // when
    pointService.addToQueueByCharge(id, amount);
    pointService.addToQueueByUse(id, amount);
    pointService.queueOperation();
    List<PointHistoryDTO> pointHistory = pointService.history(id);
    // then
    pointHistory.stream().filter(history ->
        history.type() == TransactionType.USE).forEach(history -> {
      assertThat(history.amount()).isEqualTo(amount);
      assertThat(history.type()).isEqualTo(TransactionType.USE);
    });
  }


  /**
   * history 메소드에서 임의의 충전과 출금작업을 통해 충전이력과 출금이력이 출력되는지 확인합니다.
   */
  @Test
  void history_after_charged_and_use_then_history_results(){
    // given
    long id = 1;
    long amount = 100;
    // when
    pointService.addToQueueByCharge(id, amount);
    pointService.addToQueueByUse(id, amount);
    pointService.queueOperation();
    List<PointHistoryDTO> pointHistory = pointService.history(id);
    // then
    pointHistory.forEach(history -> {
      assertThat(history.amount()).isEqualTo(amount);
      assertThat(history.type()).isIn(TransactionType.CHARGE, TransactionType.USE);
    });
  }
  /**
   * charge 메소드에서 충전하고나서 조회값이 정상적으로 반영되는지 확인합니다.
   */
  @Test
  void charge_after_charge_then_point_results() {
    // given
    long id = 1;
    long amount = 100;
    // when
    pointService.addToQueueByCharge(id, amount);
    pointService.queueOperation();

    UserPointDTO userPoint = pointService.futureMapListener(id);
    // then
    assertEquals(amount, userPoint.getPoint());
  }

}