package io.hhplus.tdd.point.service.charge;

import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.dto.UserPointDTO;
import io.hhplus.tdd.point.repository.PointRepository;
import io.hhplus.tdd.point.TransactionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
public class ChargeImpl implements ChargeSpecification {
  private final PointRepository pointRepository;

  public ChargeImpl(PointRepository pointRepository) {
    this.pointRepository = pointRepository;
  }

  @Override
  public void chargeProcess(UserPointDTO userPointDTO) {
    UserPoint userPoint = UserPointDTO.converToEntity(userPointDTO);
    if (userPoint.point() < 0) {
      throw new IllegalArgumentException("Amount must be positive");
    }
    long currentAmount =
        pointRepository
                .selectById(userPoint.id())
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .point()
            + userPointDTO.point();
    if (currentAmount == Long.MIN_VALUE) {
      log.error("Amount exceeds Long.MAX_VALUE");
      throw new IllegalArgumentException("Amount exceeds Long.MAX_VALUE");
    }
    pointRepository.insertOrUpdate(userPoint.id(), currentAmount);
    pointRepository.insertHistory(userPoint.id(), userPoint.point(), TransactionType.CHARGE, Instant.now().toEpochMilli());
  }
}
