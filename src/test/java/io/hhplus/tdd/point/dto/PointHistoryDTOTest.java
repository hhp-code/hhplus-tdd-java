package io.hhplus.tdd.point.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.List;

import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PointHistoryDTOTest {
  @Test
  @DisplayName("PointHistoryDTO DTO 변환 테스트")
  void convertToDTO_when_results_are_valid() {
    // given
    long id = 1L;
    long userId = 1L;
    long amount = 100L;
    TransactionType type = TransactionType.CHARGE;
    long updateMillis = Instant.now().toEpochMilli();
    List<PointHistory> pointHistories = List.of(new PointHistory(id, userId,amount, type ,updateMillis));
    // when
    List<PointHistoryDTO> pointHistoryDTOResult =
        PointHistoryDTO.convertToDTO(pointHistories);
    PointHistoryDTO pointHistoryDTO = pointHistoryDTOResult.get(0);
    // then
    assertEquals(id, pointHistoryDTO.id());
    assertEquals(amount, pointHistoryDTO.point());
    assertEquals(type, pointHistoryDTO.type());
    assertEquals(updateMillis, pointHistoryDTO.updateMillis());
  }
  // 데이터베이스에서 처리하기위해 Entity로 변환하는 테스트
  @Test
  @DisplayName("PointHistoryDTO Entity 변환 테스트")
  void convertToEntity_when_results_are_valid() {
    // given
    long userId = 1L;
    long amount = 100L;
    TransactionType type = TransactionType.CHARGE;
    long updateMillis = Instant.now().toEpochMilli();
    List<PointHistoryDTO> pointHistoryDTOs = List.of(new PointHistoryDTO(userId, amount, type, updateMillis));
    // when
    List<PointHistory> pointHistories = PointHistoryDTO.convertToListEntity(pointHistoryDTOs);
    PointHistory pointHistory = pointHistories.get(0);
    // then
    System.out.println( pointHistory.amount());
    assertEquals(userId, pointHistory.userId());
    assertEquals(amount, pointHistory.amount());
    assertEquals(type, pointHistory.type());
    assertEquals(updateMillis, pointHistory.updateMillis());
  }


}
