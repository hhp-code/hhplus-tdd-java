package io.hhplus.tdd.point.service.use;

import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.dto.UserPointDTO;
import io.hhplus.tdd.point.repository.PointRepository;
import io.hhplus.tdd.point.repository.PointRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UseImplTest {
    private final PointRepository pointRepository = new PointRepositoryImpl();
    private final UseImpl useImpl = new UseImpl(pointRepository);

    @Test
    @DisplayName("사용 메소드가 정상적으로 저장되는지 확인합니다.")
    void useProcess_and_verify_amounts() {
        // given
        long id = 1;
        long amount = 100;
        UserPointDTO userPoint = new UserPointDTO(id, amount);
        // when
        pointRepository.insertOrUpdate(id, amount);
        useImpl.useProcess(userPoint);
        // then
        var resultPoint = pointRepository.selectById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        assertEquals(resultPoint.point(), 0);
    }
}
