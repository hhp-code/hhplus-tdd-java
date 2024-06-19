package io.hhplus.tdd.point.repository;


import io.hhplus.tdd.point.PointHistoryDTO;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPointDTO;

import java.util.List;
import java.util.Optional;

public interface PointRepository {
  void insertOrUpdate(long id, long amount);
  void insertHistory(long id, long amount, TransactionType type, long updateMillis);
  Optional<UserPointDTO> selectById(long id);
  Optional<List<PointHistoryDTO>> selectHistories(long id);
}
