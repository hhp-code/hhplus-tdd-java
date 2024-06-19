package io.hhplus.tdd.point.repository;


import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import java.util.List;
import java.util.Optional;

import io.hhplus.tdd.point.*;
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
  public Optional<UserPointDTO> selectById(long id) {
    UserPoint userPoint = userPointTable.selectById(id);
    UserPointDTO userPointDTO = new UserPointDTO().convertToDTO(userPoint);
    return Optional.of(userPointDTO);
  }

  @Override
  public Optional<List<PointHistoryDTO>> selectHistories(long id) {
    List<PointHistory> pointHistories = pointHistoryTable.selectAllByUserId(id);
    List<PointHistoryDTO> pointHistoryDTOS = new PointHistoryDTO().convertToDTO(pointHistories);
    return Optional.of(pointHistoryDTOS);
  }

}
