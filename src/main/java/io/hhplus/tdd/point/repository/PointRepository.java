package io.hhplus.tdd.point.repository;


import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.domain.TransactionType;

import java.util.List;
import java.util.Optional;

public interface PointRepository {
  /**
   * 사용자 포인트를 삽입 또는 업데이트
   * @param id : 사용자 아이디
   * @param amount : 포인트
   */
  UserPoint insertOrUpdate(long id, long amount);

  /**
   * 포인트 히스토리를 삽입
   * @param id : 사용자 아이디
   * @param amount : 포인트
   * @param type : 트랜잭션 타입
   * @param updateMillis : 업데이트 시간
   */
  PointHistory insertHistory(long id, long amount, TransactionType type, long updateMillis);

  /**
   * 사용자 아이디로 사용자 포인트 조회
   * @param id : 사용자 아이디
   * @return Optional<UserPoint>  : 사용자 포인트 도메인 객체
   */
  UserPoint getById(long id);

  /**
   * 사용자 아이디로 포인트 히스토리 조회
   * @param id : 사용자 아이디
   * @return Optional<List<PointHistory>> : 포인트 히스토리 도메인 객체 리스트
   */
  List<PointHistory> getHistories(long id);
}
