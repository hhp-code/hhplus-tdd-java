package io.hhplus.tdd.point.dto;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.TransactionType;
import lombok.Builder;

/**
 * 포인트 히스토리 조회 데이터 객체
 * @param id : 사용자 아이디
 * @param point : 포인트
 * @param type : 트랜잭션 타입
 * @param updateMillis : 업데이트 시간
 */
public record PointHistoryDTO(long id, long point, TransactionType type, long updateMillis) {
  @Builder
  public PointHistoryDTO {
    if(id<0){
        throw new IllegalArgumentException("Id must be positive");
    }
    if(point < 0) {
      throw new IllegalArgumentException("Amount must be non-negative");
    }
  }

  public static List<PointHistoryDTO> convertToDTO(List<PointHistory> pointHistories) {
    return pointHistories.stream()
        .map(pointHistory -> new PointHistoryDTO(
            pointHistory.id(),
            pointHistory.amount(),
            pointHistory.type(),
            pointHistory.updateMillis()))
        .collect(Collectors.toList());
  }


  public static List<PointHistory> convertToListEntity(List<PointHistoryDTO> pointHistoryDTOs) {
    AtomicInteger cursor = new AtomicInteger(0);
    return pointHistoryDTOs.stream()
        .map(pointHistoryDTO -> new PointHistory(
            cursor.getAndIncrement(),
            pointHistoryDTO.id(),
            pointHistoryDTO.point(),
            pointHistoryDTO.type(),
            pointHistoryDTO.updateMillis()))
        .collect(Collectors.toList());
  }
}
