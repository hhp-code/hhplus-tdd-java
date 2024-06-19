package io.hhplus.tdd.point.service.use;

import io.hhplus.tdd.point.repository.PointRepository;
import io.hhplus.tdd.point.TransactionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
public class UseImpl implements UseSpecification{
    private final PointRepository pointRepository;

    public UseImpl(PointRepository pointRepository) {
        this.pointRepository = pointRepository;
    }

    @Override
    public  void useProcess(long id, long amount) {
        if (amount < 0 ) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        long currentAmount = pointRepository.selectById(id).orElseThrow(()-> new IllegalArgumentException("User not found")).point() -amount;
        if (currentAmount < 0) {
            throw new IllegalArgumentException("Amount exceeds balance");
        }
        pointRepository.insertOrUpdate(id, currentAmount);
        pointRepository.insertHistory(id, amount, TransactionType.USE, System.currentTimeMillis());
    }
}
