package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.dto.UserPointDTO;
import io.hhplus.tdd.point.domain.TransactionType;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UseImpl  {

  private final QueueManager queueManager;
  public UseImpl(QueueManager queueManager) {
    this.queueManager = queueManager;
  }

  /**
   * 포인트 사용 프로세스
   * @param userPointDTO : 사용자 아이디와 포인트를 담은 데이터 객체
   */
  public CompletableFuture<UserPointDTO> useProcess(UserPointDTO userPointDTO) {
    return queueManager.addToQueue(userPointDTO, TransactionType.USE);
  }


}
