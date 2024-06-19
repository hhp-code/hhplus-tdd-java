package io.hhplus.tdd.point.service;

import java.util.List;

import io.hhplus.tdd.point.PointHistoryDTO;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPointDTO;
import io.hhplus.tdd.point.service.history.HistorySpecification;
import io.hhplus.tdd.point.service.point.PointImpl;
import io.hhplus.tdd.point.service.point.PointSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PointService {

  private final QueueManager queueManager;
  private final PointSpecification pointImpl;
  private final HistorySpecification historyImpl;

  public PointService(
      QueueManager queueManager, PointImpl pointImpl, HistorySpecification historyImpl) {
    this.queueManager = queueManager;
    this.pointImpl = pointImpl;
    this.historyImpl = historyImpl;
  }

  // 포인트 조회
  public UserPointDTO point(long id) {
    return pointImpl.point(id);
  }

  // 포인트 히스토리 조회
  public List<PointHistoryDTO> history(long id) {
    return historyImpl.history(id);
  }

  // 컨트롤러 단에서 받아온 충전 요청을 큐에 추가 및 비동기 결과값 대기
  public UserPointDTO charge(long id, long amount) {
    return queueManager.handleRequest(
        () -> queueManager.addToQueue(id, amount, TransactionType.CHARGE));
  }

  // 컨트롤러 단에서 받아온 포인트 사용 요청을 큐에 추가 및 비동기 결과값 대기
  public UserPointDTO use(long id, long amount) {
    return queueManager.handleRequest(
        () -> queueManager.addToQueue(id, amount, TransactionType.USE));
  }

  // 에러 메시지 출력 및 예외 발생
  static void errorMessageThrowing(String message) {
    log.error(message);
    throw new IllegalArgumentException(message);
  }
}
