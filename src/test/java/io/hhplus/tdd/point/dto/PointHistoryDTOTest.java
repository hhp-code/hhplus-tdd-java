package io.hhplus.tdd.point.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import org.junit.jupiter.api.Test;

class PointHistoryDTOTest {
  //데이터 베이스에서 처리한 값을 DTO로 변환하는 테스트
  @Test
  void convertToDTO_when_results_are_valid() {
    // given
    long id = 1L;
    long userId = 1L;
    long amount = 100L;
    TransactionType type = TransactionType.CHARGE;
    long updateMillis = System.currentTimeMillis();
    List<PointHistory> pointHistories = List.of(new PointHistory(id, userId,amount, type ,updateMillis));
    // when
    List<PointHistoryDTO> pointHistoryDTOResult =
        PointHistoryDTO.convertToDTO(pointHistories);
    PointHistoryDTO pointHistoryDTO = pointHistoryDTOResult.get(0);
    // then
    assertEquals(id, pointHistoryDTO.id());
    assertEquals(amount, pointHistoryDTO.amount());
    assertEquals(type, pointHistoryDTO.type());
    assertEquals(updateMillis, pointHistoryDTO.updateMillis());
  }

}
