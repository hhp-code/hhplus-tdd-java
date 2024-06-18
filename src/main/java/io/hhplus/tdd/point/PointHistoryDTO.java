package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class PointHistoryDTO {
  private long id;
  private long amount;
  private TransactionType type;
  private long updateMillis;

  public PointHistoryDTO(long id, long amount, TransactionType type, long updateMillis) {
    this.id = id;
    this.amount = amount;
    this.type = type;
    this.updateMillis = updateMillis;
  }

  public PointHistoryDTO() {}

  public List<PointHistoryDTO> convertToDTO(List<PointHistory> pointHistories) {
    return pointHistories.stream()
        .map(pointHistory -> new PointHistoryDTO(
            pointHistory.id(),
            // 수정된 부분
            pointHistory.amount(),
            pointHistory.type(),
            pointHistory.updateMillis()))
        .collect(Collectors.toList());
  }


}
