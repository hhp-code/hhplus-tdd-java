package io.hhplus.tdd.point;


import java.util.List;

public interface PointRepository {
  void insertOrUpdate(long id, long amount);
  void insertHistory(long id, long amount, TransactionType type, long updateMillis);
  UserPointDTO selectById(long id);
  List<PointHistoryDTO> selectHistories(long id);
}
