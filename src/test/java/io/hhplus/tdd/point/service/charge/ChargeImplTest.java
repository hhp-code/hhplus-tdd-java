package io.hhplus.tdd.point.service.charge;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.dto.UserPointDTO;
import io.hhplus.tdd.point.repository.PointRepository;
import io.hhplus.tdd.point.repository.PointRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChargeImplTest {

    private final PointRepository pointRepository = new PointRepositoryImpl();

    @Test
    @DisplayName("충전 메소드가 정상적으로 저장되는지 확인합니다.")
    void chargeProcess_and_verify_amounts() {
        // given
        long id = 1;
        long amount = 100;
        UserPointDTO userPointDTO = new UserPointDTO(id, amount);
        // when
        ChargeImpl chargeImpl = new ChargeImpl(pointRepository);
        chargeImpl.chargeProcess(userPointDTO);
        UserPoint userPoint = pointRepository.selectById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        UserPointDTO targetDTO = UserPointDTO.convertToDTO(userPoint);
        // then
        assertEquals(userPointDTO,targetDTO);
    }
}
