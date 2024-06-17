package io.hhplus.tdd.point;

import java.sql.Time;

public class QueueEntity {
  long id;
  long amount;
  TransactionType transactionType;
  long time;

  QueueEntity(long id, long amount, TransactionType transactionType) {
    this.id = id;
    this.amount = amount;
    this.transactionType = transactionType;
    time = new Time(System.currentTimeMillis()).getTime();
  }

}
