package io.hhplus.tdd.point.service.use;

import io.hhplus.tdd.point.dto.UserPointDTO;

public interface UseSpecification {
    /**
     * 포인트 사용
     * @param userPointDTO : 사용자 아이디와 포인트를 담은 데이터 객체
     */
    void useProcess(UserPointDTO userPointDTO);

}
