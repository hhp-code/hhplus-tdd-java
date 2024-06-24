package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.dto.UserPointDTO;
import io.hhplus.tdd.point.domain.TransactionType;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChargeImpl  {

  private final QueueManager queueManager;


  public ChargeImpl(QueueManager queueManager) {
    this.queueManager = queueManager;
  }

  /**
   * 포인트 충전 프로세스
   *
   * @param userPointDTO : 사용자 아이디와 포인트를 담은 데이터 객체
   */
  public CompletableFuture<UserPointDTO> chargeProcess(UserPointDTO userPointDTO) {
    return queueManager.addToQueue(userPointDTO, TransactionType.CHARGE);
  }


}
