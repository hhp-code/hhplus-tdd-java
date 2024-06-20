package io.hhplus.tdd.point.service.history;

import io.hhplus.tdd.point.dto.PointHistoryDTO;

import java.util.List;

public interface HistorySpecification {
    /**
     * 포인트 히스토리 조회
     * @param id : 사용자 아이디
     * @return List<PointHistoryDTO> : 포인트 히스토리 조회 데이터 리스트 객체
     */
    List<PointHistoryDTO> history(long id);
}
