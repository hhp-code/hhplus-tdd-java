
package io.hhplus.tdd.point;

import java.sql.Time;
import lombok.Getter;


// 큐에 저장되는 객체를 정의한 클래스
public class QueueEntity implements Comparable<QueueEntity>{
  long id;
  long amount;
  TransactionType transactionType;
  //시간순서대로 확인하기위해 timestamp 추가
  @Getter
  long timestamp;

  QueueEntity(long id, long amount, TransactionType transactionType) {
    this.id = id;
    this.amount = amount;
    this.transactionType = transactionType;
    timestamp = new Time(System.currentTimeMillis()).getTime();
  }

  // 큐에 저장된 객체를 시간순서대로 정렬하기 위해 Comparable 인터페이스 구현
  @Override
  public int compareTo(QueueEntity other) {
    return Long.compare(this.timestamp, other.timestamp);
  }
}

