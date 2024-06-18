package io.hhplus.tdd.point;

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



    @GetMapping("{id}")
    public ResponseEntity<UserPoint> point(
            @PathVariable long id
    ) {
      log.info("point id: {}", id);
      UserPoint userPoint = pointService.point(id);
      return ResponseEntity.ok(userPoint);
    }


    @GetMapping("{id}/histories")
    public ResponseEntity<List<PointHistory>> history(
            @PathVariable long id
    ) {
      log.info("history id: {}", id);
      List<PointHistory> history = pointService.history(id);
        return ResponseEntity.ok(history);
    }


    @PatchMapping("{id}/charge")
    public ResponseEntity<UserPoint> charge(
            @PathVariable long id,
            @RequestBody long amount
    ) {
      log.info("charge id: {}, amount: {}", id, amount);
      UserPoint charged = pointService.charge(id, amount);
      return ResponseEntity.ok(charged);
    }


    @PatchMapping("{id}/use")
    public ResponseEntity<UserPoint> use(
            @PathVariable long id,
            @RequestBody long amount
    ) {
      log.info("use id: {}, amount: {}", id, amount);
      UserPoint use = pointService.use(id, amount);
      return ResponseEntity.ok(use);
    }
}