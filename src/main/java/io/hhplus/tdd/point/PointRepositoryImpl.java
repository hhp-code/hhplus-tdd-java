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
  public UserPointDTO selectById(long id) {
    UserPoint userPoint = userPointTable.selectById(id);
    return new UserPointDTO().convertToDTO(userPoint);
  }

  @Override
  public List<PointHistoryDTO> selectHistories(long id) {
    List<PointHistory> pointHistories = pointHistoryTable.selectAllByUserId(id);
    return new PointHistoryDTO().convertToDTO(pointHistories);
  }

}
