package io.hhplus.tdd.point.dto;

import io.hhplus.tdd.point.UserPoint;
import lombok.Builder;

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

  public static UserPointDTO convertToDTO(UserPoint userPoint) {
    return UserPointDTO.builder().id(userPoint.id()).point(userPoint.point()).build();
  }

  public static UserPoint converToEntity(UserPointDTO userPointDTO) {
    // 처리가 안된 객체의 표시를 위해서 0으로 초기화
    return new UserPoint(userPointDTO.id(), userPointDTO.point(),0);
  }
}
