package io.hhplus.tdd.point;

import lombok.Getter;

@Getter
public class UserPointDTO {
    private long id;
    private long point;

    public UserPointDTO(long id, long point) {
        this.id = id;
        this.point = point;
    }

  public UserPointDTO() {

  }

  public UserPointDTO convertToDTO(UserPoint userPoint) {
    return new UserPointDTO(userPoint.id(), userPoint.point());
  }
}
