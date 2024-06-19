package io.hhplus.tdd.point.service.point;

import io.hhplus.tdd.point.dto.UserPointDTO;

public interface PointSpecification {
    UserPointDTO point(long id);
}
