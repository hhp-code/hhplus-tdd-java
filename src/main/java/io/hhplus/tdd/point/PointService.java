package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PointService {

  private final UserPointTable userPointTable;
  private final PointHistoryTable pointHistoryTable;

  public PointService(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
    this.userPointTable = userPointTable;
    this.pointHistoryTable = pointHistoryTable;
  }

  public UserPoint point(long id) {
    if(id<0 ){
      return null;
    }
    return userPointTable.selectById(id);
  }

  public List<PointHistory> history(long id) {
    return pointHistoryTable.selectAllByUserId(id);
  }

  public UserPoint charge(long id, long amount) {
    if(amount < 0) {
      throw new IllegalArgumentException("amount must be positive");
    }
    UserPoint userPoint = userPointTable.selectById(id);
    if(userPoint.point() + amount == Long.MIN_VALUE) {
      throw new IllegalArgumentException("amount is exceed Long.MAX_VALUE");
    }
    amount += userPoint.point();

    userPointTable.insertOrUpdate(id, amount);
    pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());
    return userPointTable.selectById(id);
  }

  public UserPoint use(long id, long amount) {
    if(amount < 0) {
      throw new IllegalArgumentException("amount must be positive");
    }
    UserPoint userPoint = userPointTable.selectById(id);
    if(userPoint.point() - amount < 0) {
      throw new IllegalArgumentException("amount is more than balance");
    }
    long remaining = userPoint.point() - amount;
    userPointTable.insertOrUpdate(id, remaining);
    pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis());
    return userPointTable.selectById(id);
  }
}
