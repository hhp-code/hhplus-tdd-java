package io.hhplus.tdd.point.service;


import java.time.Instant;

import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.dto.UserPointDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

// 큐에 저장되는 객체를 정의한 클래스
@Getter
@ToString
@EqualsAndHashCode
public class QueueEntity implements Comparable<QueueEntity>{
  private final UserPointDTO userPointDTO;
  private final TransactionType transactionType;
  //시간순서대로 확인하기위해 timestamp 추가
  private final long timestamp;

  QueueEntity(UserPointDTO userPointDTO, TransactionType transactionType) {
    if (userPointDTO.id() < 0) {
      throw new IllegalArgumentException("ID must be positive");
    }
    if (userPointDTO.point() < 0) {
      throw new IllegalArgumentException("Amount must be non-negative");
    }
    if (transactionType == null) {
      throw new IllegalArgumentException("TransactionType must not be null");
    }
    this.userPointDTO = userPointDTO;
    this.transactionType = transactionType;
    timestamp = Instant.now().toEpochMilli();
  }

  // 큐에 저장된 객체를 시간순서대로 정렬하기 위해 Comparable 인터페이스 구현
  @Override
  public int compareTo(QueueEntity other) {
    return Long.compare(this.timestamp, other.timestamp);
  }

}