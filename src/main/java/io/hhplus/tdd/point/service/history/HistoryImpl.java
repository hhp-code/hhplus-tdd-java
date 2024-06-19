package io.hhplus.tdd.point.service.history;

import io.hhplus.tdd.point.dto.PointHistoryDTO;
import io.hhplus.tdd.point.repository.PointRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
@Slf4j
@Component
public class HistoryImpl implements HistorySpecification {
    private final PointRepository pointRepository;

    public HistoryImpl(PointRepository pointRepository) {
        this.pointRepository = pointRepository;
    }

    @Override
    public List<PointHistoryDTO> history(long id) {
        return pointRepository
                .selectHistories(id)
                .orElseThrow(
                        () -> {
                            log.error("server error");
                            return new IllegalArgumentException("server error");
                        });
    }
}
