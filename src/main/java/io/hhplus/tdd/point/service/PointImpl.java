package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.dto.UserPointDTO;
import io.hhplus.tdd.point.repository.PointRepository;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PointImpl {

  private final PointRepository pointRepository;
  private AtomicReference<UserPoint> userPointRef = new AtomicReference<>();
  public PointImpl(PointRepository pointRepository) {
    this.pointRepository = pointRepository;
  }

  /**
   * 포인트 조회 프로세스
   *
   * @param id : 사용자 아이디
   * @return UserPointDTO : 사용자 아이디와 포인트를 담은 데이터 객체
   */
  public UserPointDTO point(long id) {
    if (id < 0) {
      throw new IllegalArgumentException("Id must be positive");
    }
    UserPoint userPoint = userPointRef.get();
    if (userPoint == null) {
      userPoint = pointRepository.getById(id);
      userPointRef.set(userPoint);
    }
    return UserPointDTO.convertToDTO(userPoint);
  }
}
