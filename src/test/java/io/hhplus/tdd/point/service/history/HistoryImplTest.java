package io.hhplus.tdd.point.service.history;

import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.repository.PointRepository;
import io.hhplus.tdd.point.repository.PointRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class HistoryImplTest {
  private final PointRepository pointRepository = new PointRepositoryImpl();

    @Test
    @DisplayName("포인트 히스토리 조회 결과값이 없을때")
    void history_then_no_results() {
        // given
        HistoryImpl historyImpl = new HistoryImpl(pointRepository);
        long id = 1L;

        // when
        var result = historyImpl.history(id);

        // then
        assertNotNull(result);
    }
    @Test
    @DisplayName("포인트 히스토리 조회 결과값이 있을때")
    void history_then_results(){
        // given
        HistoryImpl historyImpl = new HistoryImpl(pointRepository);
        long id = 1L;
        long differentId = 2L;
        pointRepository.insertHistory(id, 100, TransactionType.CHARGE, 1000L);
        pointRepository.insertHistory(id, 200, TransactionType.USE, 2000L);
        pointRepository.insertHistory(differentId, 300, TransactionType.CHARGE, 3000L);
        // when
        var result = historyImpl.history(id);

        // then
        assertThat(result).hasSize(2);
    }
}
