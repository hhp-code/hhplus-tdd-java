package io.hhplus.tdd.point.service.history;

import io.hhplus.tdd.point.dto.PointHistoryDTO;

import java.util.List;

public interface HistorySpecification {
    List<PointHistoryDTO> history(long id);
}
