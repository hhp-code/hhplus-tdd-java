package io.hhplus.tdd.point.repository;


import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.TransactionType;

import java.util.List;
import java.util.Optional;

public interface PointRepository {
  void insertOrUpdate(long id, long amount);
  void insertHistory(long id, long amount, TransactionType type, long updateMillis);
  Optional<UserPoint> selectById(long id);
  Optional<List<PointHistory>> selectHistories(long id);
}
