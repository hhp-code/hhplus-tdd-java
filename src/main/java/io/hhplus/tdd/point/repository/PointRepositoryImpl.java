package io.hhplus.tdd.point.repository;


import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import java.util.List;

import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.domain.UserPoint;
import org.springframework.stereotype.Repository;

@Repository
public class PointRepositoryImpl implements PointRepository{
  private final PointHistoryTable pointHistoryTable;
  private final UserPointTable userPointTable;

  public PointRepositoryImpl(PointHistoryTable pointHistoryTable, UserPointTable userPointTable) {
    this.pointHistoryTable = pointHistoryTable;
    this.userPointTable = userPointTable;
  }


  /**
   * 사용자 포인트를 삽입 또는 업데이트
   * @param id : 사용자 아이디
   * @param amount : 포인트
   */
  @Override
  public UserPoint insertOrUpdate(long id, long amount) {
    return userPointTable.insertOrUpdate(id, amount);
  }

  /**
   * 포인트 히스토리를 삽입
   * @param id : 사용자 아이디
   * @param amount : 포인트
   * @param type : 트랜잭션 타입
   * @param updateMillis : 업데이트 시간
   */
  @Override
  public PointHistory insertHistory(long id, long amount, TransactionType type, long updateMillis) {
    return pointHistoryTable.insert(id, amount, type, updateMillis);
  }

  /**
   * 사용자 아이디로 사용자 포인트 조회
   * @param id : 사용자 아이디
   * @return Optional<UserPoint>  : 사용자 포인트 도메인 객체
   */
  @Override
  public UserPoint getById(long id) {
    return userPointTable.selectById(id);
  }

  /**
   * 사용자 아이디로 포인트 히스토리 조회
   * @param id : 사용자 아이디
   * @return Optional<List<PointHistory>> : 포인트 히스토리 도메인 객체 리스트
   */
  @Override
  public List<PointHistory> getHistories(long id) {
    return pointHistoryTable.selectAllByUserId(id);
  }

}
