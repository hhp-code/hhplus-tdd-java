package io.hhplus.tdd.point.dto;

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
        UserPointDTO userPointDTO = new UserPointDTO(id, point);
        // then
        assertEquals(id, userPointDTO.id());
        assertEquals(point, userPointDTO.point());
    }

}
