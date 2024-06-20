package io.hhplus.tdd.point.service.charge;

import io.hhplus.tdd.point.dto.UserPointDTO;

public interface ChargeSpecification {
    void chargeProcess(UserPointDTO userPointDTO);
}
