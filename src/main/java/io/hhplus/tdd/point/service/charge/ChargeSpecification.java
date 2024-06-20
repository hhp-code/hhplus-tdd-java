package io.hhplus.tdd.point.service.charge;

import io.hhplus.tdd.point.dto.UserPointDTO;

public interface ChargeSpecification {
    /**
     * 포인트 충전
     * @param userPointDTO : 사용자 아이디와 포인트를 담은 데이터 객체
     */
    void chargeProcess(UserPointDTO userPointDTO);
}
