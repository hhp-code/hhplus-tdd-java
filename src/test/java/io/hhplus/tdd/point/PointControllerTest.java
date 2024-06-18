package io.hhplus.tdd.point;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PointController.class)
class PointControllerTest {
  @Autowired MockMvc mockMvc;
  @MockBean private PointService pointService;

  @ParameterizedTest
  @MethodSource("randomValueSupplier")
  void point(long id) throws Exception {
    //given

    //when
    UserPoint userPoint = new UserPoint(id, 0, System.currentTimeMillis());
    when(pointService.point(id)).thenReturn(userPoint);
    //then
    mockMvc
        .perform(get("/point/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id))
        .andExpect(jsonPath("$.point").value(0))
        .andExpect(jsonPath("$.updateMillis").isNumber());
    verify(pointService).point(id);
  }

  static Stream<Long> randomValueSupplier(){
    return Stream.generate(() -> (long) (Math.random() * Long.MAX_VALUE)).limit(100);
  }
  @ParameterizedTest
  @MethodSource("randomValueSupplier")
  void history(long id) throws Exception {
    //given
    //when
    when(pointService.history(id)).thenReturn(List.of());

    //then
    mockMvc
        .perform(get("/point/{id}/histories", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
    verify(pointService).history(id);
  }

  static Stream<Arguments> randomValueMultiSupplier(){
    Random random = new Random();
    return Stream.generate(() -> Arguments.of(random.nextLong(), random.nextLong()))
        .limit(100);

  }

  @ParameterizedTest
  @MethodSource("randomValueMultiSupplier")
  void charge(long userId, long amount) throws Exception {
    //given

    //when
    UserPoint userPoint = new UserPoint(userId, amount, System.currentTimeMillis());
    when(pointService.charge(userId, amount)).thenReturn(userPoint);

    //then
    mockMvc
        .perform(
            patch("/point/{id}/charge", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(amount)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId))
        .andExpect(jsonPath("$.point").value(amount));
    verify(pointService).charge(userId, amount);
  }

  @ParameterizedTest
  @MethodSource("randomValueMultiSupplier")
  void use(long userId, long amount) throws Exception {
    //given

    //when
    UserPoint userPoint = new UserPoint(userId, amount, System.currentTimeMillis());
    when(pointService.use(userId, amount)).thenReturn(userPoint);

    //then
    mockMvc
        .perform(
            patch("/point/{id}/use", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(amount)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId))
        .andExpect(jsonPath("$.point").value(amount));
    verify(pointService).use(userId, amount);
  }
}
