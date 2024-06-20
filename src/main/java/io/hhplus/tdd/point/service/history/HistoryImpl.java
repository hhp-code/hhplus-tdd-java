package io.hhplus.tdd.point.service.history;

import io.hhplus.tdd.point.domain.PointHistory;
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

    /**
     * 포인트 히스토리 조회 프로세스
     * @param id : 사용자 아이디
     * @return List<PointHistoryDTO> : 포인트 히스토리 조회 데이터 리스트 객체
     */
    @Override
    public List<PointHistoryDTO> history(long id) {
        List<PointHistory> pointHistories = pointRepository
                .selectHistories(id)
                .orElseThrow(
                        () -> {
                            log.error("server error");
                            return new IllegalArgumentException("server error");
                        });
        return PointHistoryDTO.convertToDTO(pointHistories);
    }
}
