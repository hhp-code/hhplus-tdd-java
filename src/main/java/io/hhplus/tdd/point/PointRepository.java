package io.hhplus.tdd.point;


import java.util.List;
import java.util.Optional;

public interface PointRepository {
  void insertOrUpdate(long id, long amount);
  void insertHistory(long id, long amount, TransactionType type, long updateMillis);
  Optional<UserPointDTO> selectById(long id);
  Optional<List<PointHistoryDTO>> selectHistories(long id);
}
