package io.hhplus.tdd.point.dto;

import io.hhplus.tdd.point.domain.UserPoint;
import lombok.Builder;

/**
 * 사용자 아이디와 포인트를 담은 데이터 객체
 * @param id : 사용자 아이디
 * @param point : 포인트
 */
public record UserPointDTO(long id, long point) {
  @Builder
  public UserPointDTO {
    if(id<0){
        throw new IllegalArgumentException("Id must be positive");
    }
    if(point < 0) {
      throw new IllegalArgumentException("Amount must be non-negative");
    }
  }

  /**
   * UserPointDTO 객체를 UserPoint 객체로 변환
   * @param userPoint : UserPoint 객체
   * @return UserPointDTO : UserPointDTO 객체
   */
  public static UserPointDTO convertToDTO(UserPoint userPoint) {
    return UserPointDTO.builder().id(userPoint.id()).point(userPoint.point()).build();
  }

  /**
   * UserPointDTO 객체를 UserPoint 객체로 변환
   * @param userPointDTO : UserPointDTO 객체
   * @return UserPoint : UserPoint 객체
   */
  public static UserPoint convertToEntity(UserPointDTO userPointDTO) {
    // 처리가 안된 객체의 표시를 위해서 0으로 초기화
    return new UserPoint(userPointDTO.id(), userPointDTO.point(),0);
  }

}
