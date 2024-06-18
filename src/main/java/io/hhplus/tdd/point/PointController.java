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
    public ResponseEntity<UserPointDTO> point(
            @PathVariable long id
    ) {
      log.info("point getId: {}", id);
      UserPointDTO userPoint = pointService.point(id);
      return ResponseEntity.ok(userPoint);
    }


    @GetMapping("{id}/histories")
    public ResponseEntity<List<PointHistoryDTO>> history(
            @PathVariable long id
    ) {
      log.info("history getId: {}", id);
      List<PointHistoryDTO> history = pointService.history(id);
        return ResponseEntity.ok(history);
    }


    @PatchMapping("{id}/charge")
    public ResponseEntity<UserPointDTO> charge(
            @PathVariable long id,
            @RequestBody UserPointDTO userPointDTO
    ) {
      log.info("charge getId: {}, getAmount: {}", id, userPointDTO.getPoint());
    UserPointDTO charged = pointService.charge(id, userPointDTO.getPoint());
      return ResponseEntity.ok(charged);
    }


    @PatchMapping("{id}/use")
    public ResponseEntity<UserPointDTO> use(
            @PathVariable long id,
            @RequestBody UserPointDTO userPointDTO
    ) {
      log.info("use getId: {}, getAmount: {}", id, userPointDTO.getPoint());
      UserPointDTO use = pointService.use(id, userPointDTO.getPoint());
      return ResponseEntity.ok(use);
    }

}