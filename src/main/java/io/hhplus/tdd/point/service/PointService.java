package io.hhplus.tdd.point.service;

import java.util.List;

import io.hhplus.tdd.point.dto.PointHistoryDTO;
import io.hhplus.tdd.point.dto.UserPointDTO;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PointService {

  private final PointImpl pointImpl;
  private final HistoryImpl historyImpl;
  private final UseImpl useImpl;
  private final ChargeImpl chargeImpl;

  /**
   * 포인트 서비스 생성자
   * @param pointImpl : 포인트 서비스 구현체
   * @param historyImpl : 히스토리 서비스 구현체
   */
  public PointService(
      PointImpl pointImpl, HistoryImpl historyImpl, UseImpl useImpl,
      ChargeImpl chargeImpl) {
    this.pointImpl = pointImpl;
    this.historyImpl = historyImpl;
    this.useImpl = useImpl;
    this.chargeImpl = chargeImpl;
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
  public CompletableFuture<UserPointDTO> charge(UserPointDTO userPointDTO) {
    return chargeImpl.chargeProcess(userPointDTO);
  }

  // 컨트롤러 단에서 받아온 포인트 사용 요청을 큐에 추가 및 비동기 결과값 대기
  public CompletableFuture<UserPointDTO> use(UserPointDTO userPointDTO) {
    return useImpl.useProcess(userPointDTO);
  }

  // 에러 메시지 출력 및 예외 발생
  public static void errorMessageThrowing(String message) {
    log.error(message);
    throw new IllegalArgumentException(message);
  }
}
