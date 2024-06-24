package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.dto.UserPointDTO;
import io.hhplus.tdd.point.repository.PointRepository;
import io.hhplus.tdd.point.repository.PointRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PointImplTest {
    private final PointRepository pointRepository= new PointRepositoryImpl(new PointHistoryTable(), new UserPointTable());
    @Test
    @DisplayName("아이디를 음수로 넣었을때, 조회 메서드를 실행하면 IllegalArgumentException이 발생한다.")
    void point_with_negative_id_then_no_result(){
        // given
        PointImpl pointImpl = new PointImpl(pointRepository);
        long id = -1L;
        // when
        // then
        assertThrows(IllegalArgumentException.class, () -> pointImpl.point(id));
    }
    @Test
    @DisplayName("아이디가 양수일때 포인트 조회 결과값이 없을때")
    void point_with_positive_id_then_no_result(){
        // given
        PointImpl pointImpl = new PointImpl(pointRepository);
        long id = 1L;
        // when
        UserPointDTO point = pointImpl.point(id);
        // then
        assertThat(point.point()).isEqualTo(0);

    }

    @Test
    @DisplayName("아이디가 양수일때 포인트 조회 결과값이 있을때")
    void point_then_result(){
        //given
        PointImpl pointImpl = new PointImpl(pointRepository);
        long id = 1L;
        //when
        pointRepository.insertOrUpdate(id, 100);
        //then
        assertThat(pointImpl.point(id).point()).isEqualTo(100);
    }
}
