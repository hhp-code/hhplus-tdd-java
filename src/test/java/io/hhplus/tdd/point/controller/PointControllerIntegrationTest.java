package io.hhplus.tdd.point.controller;

import io.hhplus.tdd.point.dto.PointHistoryDTO;
import io.hhplus.tdd.point.dto.UserPointDTO;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 포인트 컨트롤러 통합 테스트 : SpringBootTest를 활용했고, 랜덤한 포트로 테스트를 진행합니다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PointControllerIntegrationTest {

  // * RestTemplate, TestRestTemplate 는 3.2 에서 적절하지 않으므로 RestClient를 여러 시도끝에 사용하게되었습니다.
  private RestClient restClient;
  // * 랜덤 포트를 끌어오기 위한 어노테이션
  @LocalServerPort
  private int port;

  // * 테스트 시작 전에 포트를 끌어오고, RestClient를 생성합니다.
  @BeforeEach
  public void setUp() {
    String baseUrl = "http://localhost:" + port;
    restClient = RestClient.create(baseUrl);
  }

  // * 포인트 조회 테스트
  @Test
  public void testGetPoint() {
    long id = 1;
    ResponseEntity<UserPointDTO> response =
        // * get 메소드를 사용하여 /point/{id}로 요청을 보냅니다.
        restClient
            .get()
            .uri("/point/{id}", id)
            // * Accept 헤더를 JSON으로 설정합니다.
            .accept(MediaType.APPLICATION_JSON)
            // * retrieve 메소드를 사용하여 ResponseEntity로 변환합니다.
            .retrieve()
            // * toEntity 메소드를 사용하여 UserPointDTO로 변환합니다.
            .toEntity(UserPointDTO.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().id()).isEqualTo(id);
  }

  // * 포인트 히스토리 조회 테스트
  @Test
  public void testGetHistory() {
    long id = 1;
    ResponseEntity<PointHistoryDTO[]> response =
        restClient
            .get()
            .uri("/point/{id}/histories", id)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .toEntity(PointHistoryDTO[].class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
  }

  // * 포인트 충전 테스트
  @Test
  public void testChargePoint() {
    long id = 1;
    AtomicLong Id = new AtomicLong(1);
    AtomicLong point = new AtomicLong(100);
    UserPointDTO userPointDTO = new UserPointDTO(Id, point);
    ResponseEntity<UserPointDTO> response =
        restClient
            .patch()
            .uri("/point/{id}/charge", id)
            .contentType(MediaType.APPLICATION_JSON)
            .body(userPointDTO)
            .retrieve()
            .toEntity(UserPointDTO.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().id()).isEqualTo(id);
    assertThat(response.getBody().point()).isEqualTo(100);
  }

  // * 포인트 사용 테스트
  @Test
  public void testUsePoint() {
    long id = 1;
    AtomicLong Id = new AtomicLong(1);
    AtomicLong chargePoint = new AtomicLong(100);
    AtomicLong usePoint = new AtomicLong(50);
    UserPointDTO chargeDTO = new UserPointDTO(Id, chargePoint);

    UserPointDTO userPointDTO = new UserPointDTO(Id, usePoint);
    RestClient testRestClient;
    testRestClient = RestClient.create("http://localhost:" + port);
    // * ExecutorService를 사용하여 두 개의 스레드를 생성합니다.
    ExecutorService executorService = Executors.newFixedThreadPool(2);
    // * CountDownLatch를 사용하여 스레드의 순서를 보장합니다.
    CountDownLatch latch = new CountDownLatch(1);

    executorService.submit(
        () -> {
          try {
            // * charge 작업을 수행합니다.
            testRestClient
                .patch()
                .uri("/point/{id}/charge", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(chargeDTO)
                .retrieve()
                .toEntity(UserPointDTO.class);
            latch.countDown(); // charge 작업 완료 후 countDown 호출
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });

    executorService.submit(
        () -> {
          try {
            latch.await(); // charge 작업이 완료될 때까지 대기
            // * use 작업을 수행합니다.
            ResponseEntity<UserPointDTO> response =
                testRestClient
                    .patch()
                    .uri("/point/{id}/use", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(userPointDTO)
                    .retrieve()
                    .toEntity(UserPointDTO.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(Objects.requireNonNull(response.getBody()).id()).isEqualTo(id);
            assertThat(Objects.requireNonNull(response.getBody()).point()).isEqualTo(50);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        });

    // * ExecutorService를 종료합니다.
    executorService.shutdown();
  }
}
