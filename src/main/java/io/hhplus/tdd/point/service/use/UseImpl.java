package io.hhplus.tdd.point.service.use;

import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.dto.UserPointDTO;
import io.hhplus.tdd.point.repository.PointRepository;
import io.hhplus.tdd.point.TransactionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
public class UseImpl implements UseSpecification {
  private final PointRepository pointRepository;

  public UseImpl(PointRepository pointRepository) {
    this.pointRepository = pointRepository;
  }

  @Override
  public void useProcess(UserPointDTO userPointDTO) {
    UserPoint userPoint = UserPointDTO.converToEntity(userPointDTO);
    if (userPoint.point() < 0) {
      throw new IllegalArgumentException("Amount must be positive");
    }
    long currentAmount =
        pointRepository
                .selectById(userPoint.id())
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .point()
            - userPoint.point();
    if (currentAmount < 0) {
      throw new IllegalArgumentException("Amount exceeds balance");
    }
    pointRepository.insertOrUpdate(userPoint.id(), currentAmount);
    pointRepository.insertHistory(
        userPoint.id(), userPoint.point(), TransactionType.USE, Instant.now().toEpochMilli());
  }
}
