package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.domain.UserPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointRepositoryImplTest {

  @Mock UserPointTable userPointTable;

  @Mock PointHistoryTable pointHistoryTable;

  @InjectMocks PointRepositoryImpl pointRepository;

  @Test
  @DisplayName("사용자 아이디로 사용자 포인트 조회")
  void getById() {
    // given
    long id = 1L;
    UserPoint userPoint = new UserPoint(id, 100L, 1L);
    // when
    when(userPointTable.selectById(id)).thenReturn(userPoint);

    // then
    assertNotNull(pointRepository.getById(id));
    verify(userPointTable).selectById(id);
  }

  @Test
  @DisplayName("사용자 아이디로 포인트 히스토리 조회")
  void getHistories() {
    // given
    long id = 1L;
    PointHistory pointHistory = new PointHistory(id, id, 100L, TransactionType.CHARGE, 1L);
    List<PointHistory> pointHistories = List.of(pointHistory);
    // when
    when(pointHistoryTable.selectAllByUserId(id)).thenReturn(pointHistories);
    List<PointHistory> result = pointRepository.getHistories(id);
    // then
    assertNotNull(result);
    verify(pointHistoryTable, times(1)).selectAllByUserId(id);
  }

  @Test
  @DisplayName("사용자 포인트를 삽입 또는 업데이트")
  void insertOrUpdate() {
    // given
    long id = 1L;
    long amount = 100L;
    // when
    UserPoint userPoint = new UserPoint(id, amount, 1L);
    when(userPointTable.insertOrUpdate(id, amount)).thenReturn(userPoint);
    pointRepository.insertOrUpdate(id, amount);

    // then
    verify(userPointTable, times(1)).insertOrUpdate(id, amount);
  }

  @Test
  @DisplayName("포인트 히스토리를 삽입")
  void insertHistory() {
    // given
    long id = 1L;
    long amount = 100L;
    TransactionType type = TransactionType.CHARGE;
    long updateMillis = 1L;

    // when
    when(pointHistoryTable.insert(id, amount, type, updateMillis))
        .thenReturn(new PointHistory(id, id, amount, type, updateMillis));
    pointRepository.insertHistory(id, amount, type, updateMillis);

    // then
    verify(pointHistoryTable, times(1)).insert(id, amount, type, updateMillis);
  }
}
