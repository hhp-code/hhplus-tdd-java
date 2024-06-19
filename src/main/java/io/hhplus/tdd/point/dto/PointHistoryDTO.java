package io.hhplus.tdd.point.dto;

import java.util.List;
import java.util.stream.Collectors;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import lombok.Builder;

public record PointHistoryDTO(long id, long amount, TransactionType type, long updateMillis) {
  @Builder
  public PointHistoryDTO {
    if (id < 0 || amount < 0) throw new IllegalArgumentException("id와 amount는 0보다 작을 수 없습니다.");
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


}
