package io.hhplus.tdd.point;

import java.sql.Time;
import lombok.Getter;

public class QueueEntity implements Comparable<QueueEntity>{
  long id;
  long amount;
  TransactionType transactionType;
  @Getter
  long timestamp;

  QueueEntity(long id, long amount, TransactionType transactionType) {
    this.id = id;
    this.amount = amount;
    this.transactionType = transactionType;
    timestamp = new Time(System.currentTimeMillis()).getTime();
  }

  @Override
  public int compareTo(QueueEntity other) {
    return Long.compare(this.timestamp, other.timestamp);
  }
}
