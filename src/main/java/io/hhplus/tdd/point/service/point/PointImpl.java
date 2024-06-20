package io.hhplus.tdd.point.service.point;

import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.dto.UserPointDTO;
import io.hhplus.tdd.point.repository.PointRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PointImpl implements PointSpecification {
  private final PointRepository pointRepository;

  public PointImpl(PointRepository pointRepository) {
    this.pointRepository = pointRepository;
  }

  @Override
  public UserPointDTO point(long id) {
    if (id < 0) {
      throw new IllegalArgumentException("Id must be positive");
    }
      UserPoint userPoint = pointRepository
              .selectById(id)
              .orElseThrow(
                      () -> {
                          log.error("server error");
                          return new IllegalArgumentException("server error");
                      });
      return UserPointDTO.convertToDTO(userPoint);
  }
}
