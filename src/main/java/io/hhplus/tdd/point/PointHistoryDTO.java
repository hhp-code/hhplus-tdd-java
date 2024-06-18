package io.hhplus.tdd.point;

import java.util.List;
import java.util.stream.Collectors;

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

  public PointHistoryDTO() {

  }

  public long id() {
        return id;
    }

    public long amount() {
        return amount;
    }

    public TransactionType type() {
        return type;
    }

    public long updateMillis() {
        return updateMillis;
    }

  public List<PointHistoryDTO> convertToDTO(List<PointHistory> pointHistories) {
    return pointHistories.stream()
        .map(pointHistory -> new PointHistoryDTO(pointHistory.id(), pointHistory.amount(), pointHistory.type(), pointHistory.updateMillis()))
        .collect(Collectors.toList());
  }
}
