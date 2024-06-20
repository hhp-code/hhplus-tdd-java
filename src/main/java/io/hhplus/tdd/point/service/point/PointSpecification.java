package io.hhplus.tdd.point.service.point;

import io.hhplus.tdd.point.dto.UserPointDTO;

public interface PointSpecification {
    /**
     * 포인트 조회
     * @param id : 사용자 아이디
     * @return UserPointDTO : 사용자 아이디와 포인트를 담은 데이터 객체
     */
    UserPointDTO point(long id);
}
