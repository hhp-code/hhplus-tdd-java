package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import java.util.List;
import org.springframework.stereotype.Repository;


@Repository
public class PointRepositoryImpl implements PointRepository{
  PointHistoryTable pointHistoryTable = new PointHistoryTable();
  UserPointTable userPointTable = new UserPointTable();

  @Override
  public void insertOrUpdate(long id, long amount) {
    userPointTable.insertOrUpdate(id, amount);
  }

  @Override
  public void insertHistory(long id, long amount, TransactionType type, long updateMillis) {
    pointHistoryTable.insert(id, amount, type, updateMillis);
  }

  @Override
  public UserPoint selectById(long id) {
    return userPointTable.selectById(id);
  }

  @Override
  public List<PointHistory> selectHistories(long id) {
    return pointHistoryTable.selectAllByUserId(id);
  }
}