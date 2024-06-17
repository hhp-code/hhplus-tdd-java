package io.hhplus.tdd.point;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PointController.class)
class PointControllerTest {
  @Autowired MockMvc mockMvc;
  @MockBean private PointService pointService;

  @Test
  void point() throws Exception {
    //given
    long id = 1L;
    //when
    UserPoint userPoint = new UserPoint(id, 0, System.currentTimeMillis());
    when(pointService.point(id)).thenReturn(userPoint);
    //then
    mockMvc
        .perform(get("/point/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.point").value(0))
        .andExpect(jsonPath("$.updateMillis").isNumber());
    verify(pointService).point(id);
  }

  @Test
  void history() throws Exception {
    //given
    long id = 1L;
    //when
    when(pointService.history(id)).thenReturn(List.of());

    //then
    mockMvc
        .perform(get("/point/{id}/histories", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
    verify(pointService).history(id);
  }

  @Test
  void charge() throws Exception {
    //given
    long userId = 1L;
    long amount = 100L;

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

  @Test
  void use() throws Exception {
    //given
    long userId = 1L;
    long amount = 100L;

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
