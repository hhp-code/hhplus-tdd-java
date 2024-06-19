package io.hhplus.tdd.point.service.charge;

import io.hhplus.tdd.point.repository.PointRepository;
import io.hhplus.tdd.point.TransactionType;
import org.springframework.stereotype.Component;

@Component
public class ChargeImpl implements ChargeSpecification {
    private final PointRepository pointRepository;

    public ChargeImpl(PointRepository pointRepository) {
        this.pointRepository = pointRepository;
    }

    @Override
    public  void chargeProcess(long id, long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        long currentAmount = pointRepository.selectById(id).orElseThrow(()-> new IllegalArgumentException("User not found")).point()+ amount;
        if (currentAmount == Long.MIN_VALUE) {
            throw new IllegalArgumentException("Amount exceeds Long.MAX_VALUE");
        }
        pointRepository.insertOrUpdate(id, currentAmount);
        pointRepository.insertHistory(id, amount, TransactionType.CHARGE, System.currentTimeMillis());
    }
}
