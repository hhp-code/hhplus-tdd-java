package io.hhplus.tdd.point.dto;

import io.hhplus.tdd.point.domain.UserPoint;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserPointDTOTest {
    @Test
    @DisplayName("UserPointDTO DTO 변환 테스트")
    void convertToDTO_when_results_are_valid() {
        // given
        long id = 1L;
        long point = 100L;
        // when
        UserPoint userPoint = new UserPoint(id, point, 0);
        UserPointDTO userPointDTO = UserPointDTO.convertToDTO(userPoint);
        // then
        assertEquals(id, userPointDTO.id());
        assertEquals(point, userPointDTO.point());
    }
    @Test
    @DisplayName("UserPointDTO Entity 변환 테스트")
    void convertToEntity_when_results_are_valid() {
        // given
        long id = 1L;
        long point = 100L;
        AtomicLong atomicId = new AtomicLong(id);
        AtomicLong atomicLong = new AtomicLong(point);

        // when
        UserPointDTO userPointDTO = new UserPointDTO(atomicId, atomicLong);
        UserPoint userPoint = UserPointDTO.convertToEntity(userPointDTO);
        // then
        assertEquals(id, userPoint.id());
        assertEquals(point, userPoint.point());
    }

}
