package io.hhplus.tdd.point.controller;

import io.hhplus.tdd.point.dto.PointHistoryDTO;
import io.hhplus.tdd.point.dto.UserPointDTO;
import io.hhplus.tdd.point.service.PointService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/point")
public class PointController {

  private static final Logger log = LoggerFactory.getLogger(PointController.class);

  private final PointService pointService;

  public PointController(PointService pointService) {
    this.pointService = pointService;
  }

  /**
   * 포인트 조회
   * @param id : 사용자 아이디
   * @return : ResponseEntity<UserPointDTO> : 사용자 아이디와 포인트를 담은 응답 객체
   */
  @GetMapping("{id}")
  public ResponseEntity<UserPointDTO> point(@PathVariable long id) {
    log.info("point getId: {}", id);
    UserPointDTO point = pointService.point(id);
    return ResponseEntity.ok(point);
  }

  /**
   * 포인트 히스토리 조회
   * @param id : 사용자 아이디
   * @return ResponseEntity<List<PointHistoryDTO>> : 포인트 히스토리 조회 응답 객체
   */
  @GetMapping("{id}/histories")
  public ResponseEntity<List<PointHistoryDTO>> history(@PathVariable long id) {
    log.info("history getId: {}", id);
    List<PointHistoryDTO> history = pointService.history(id);
    return ResponseEntity.ok(history);
  }

  /**
   * 포인트 충전
   * @param id : 사용자 아이디
   * @param userPointDTO : 사용자 아이디와 포인트를 담은 데이터 객체
   * @return ResponseEntity<UserPointDTO> : 사용자 아이디와 포인트를 담은 응답 객체
   */
  @PatchMapping("{id}/charge")
  public ResponseEntity<UserPointDTO> charge(
      @PathVariable long id, @RequestBody UserPointDTO userPointDTO) {
    log.info("Controller charge getId: {}, getAmount: {}", id, userPointDTO.point());
    UserPointDTO charge = pointService.charge(userPointDTO);
    return ResponseEntity.ok(charge);
  }

  /**
   * 포인트 사용
   * @param id : 사용자 아이디
   * @param userPointDTO : 사용자 아이디와 포인트를 담은 데이터 객체
   * @return ResponseEntity<UserPointDTO> : 사용자 아이디와 포인트를 담은 응답 객체
   */
  @PatchMapping("{id}/use")
  public ResponseEntity<UserPointDTO> use(
      @PathVariable long id, @RequestBody UserPointDTO userPointDTO) {
    log.info("use getId: {}, getAmount: {}", id, userPointDTO.point());
    UserPointDTO use = pointService.use(userPointDTO);
    return ResponseEntity.ok(use);
  }
}
