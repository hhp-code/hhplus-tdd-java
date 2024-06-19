package io.hhplus.tdd.point.dto;

import io.hhplus.tdd.point.UserPoint;
import lombok.Builder;

public record UserPointDTO(long id, long point) {
  @Builder
  public UserPointDTO {
    if (id < 0 || point < 0) throw new IllegalArgumentException("id와 point는 0보다 작을 수 없습니다.");
  }

  public static UserPointDTO convertToDTO(UserPoint userPoint) {
    return UserPointDTO.builder().id(userPoint.id()).point(userPoint.point()).build();
  }
}
