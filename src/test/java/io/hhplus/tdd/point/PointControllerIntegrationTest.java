package io.hhplus.tdd.point;

import io.hhplus.tdd.point.dto.PointHistoryDTO;
import io.hhplus.tdd.point.dto.UserPointDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PointControllerIntegrationTest {

  private RestClient restClient;
  @LocalServerPort
    private int port;


  @BeforeEach
  public void setUp() {
      String baseUrl = "http://localhost:"+port;
    restClient = RestClient.create(baseUrl);
  }

  @Test
  public void testGetPoint() {
    long id = 1;
    ResponseEntity<UserPointDTO> response =
        restClient
            .get()
            .uri("/point/{id}", id)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .toEntity(UserPointDTO.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().id()).isEqualTo(id);
  }

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

  @Test
  public void testChargePoint() {
    long id = 1;
    UserPointDTO userPointDTO = new UserPointDTO(id, 100);
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

  @Test
  public void testUsePoint() throws InterruptedException {
    long id = 1;
    UserPointDTO chargeDTO = new UserPointDTO(id, 100);

    UserPointDTO userPointDTO = new UserPointDTO(id, 50);
    //미리 충전하기
    ResponseEntity<UserPointDTO> entity = restClient
            .patch()
            .uri("/point/{id}/charge", id)
            .contentType(MediaType.APPLICATION_JSON)
            .body(chargeDTO).retrieve().toEntity(UserPointDTO.class);
    Thread.sleep(1000);
      //사용하는 예제
    ResponseEntity<UserPointDTO> response =
        restClient
            .patch()
            .uri("/point/{id}/use", id)
            .contentType(MediaType.APPLICATION_JSON)
            .body(userPointDTO)
            .retrieve()
            .toEntity(UserPointDTO.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().id()).isEqualTo(id);
    assertThat(Objects.requireNonNull(entity.getBody()).point()).isEqualTo(100);
    assertThat(response.getBody().point()).isEqualTo(50);

  }
}
