package io.hhplus.tdd.point;

import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface PointRepository {
  UserPoint insertOrUpdate(long id, long amount);
  PointHistory insertHistory(long id, long amount, TransactionType type, long updateMillis);
  UserPoint selectById(long id);
  List<PointHistory> selectHistories(long id);
}
