package io.hhplus.tdd.point;


import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

import io.hhplus.tdd.point.dto.PointHistoryDTO;
import io.hhplus.tdd.point.dto.UserPointDTO;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * 1. MockMvc를 사용하여, pointController에서 예상되는 결과값이 출력되는지 확인했습니다.
 * 2. verify를 사용하여, pointService에서 해당 메소드가 호출되었는지 확인했습니다.
 */
@WebMvcTest(PointController.class)
class PointControllerTest {
  @Autowired MockMvc mockMvc;
  @MockBean private PointService pointService;
  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  void setUp(WebApplicationContext webApplicationContext) {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

  @Test
  void point() throws Exception {
    //given
    long id = 1L;

    //when
    UserPointDTO userPoint = new UserPointDTO(id, 0);
    when(pointService.point(id)).thenReturn(userPoint);
    //then
    mockMvc
        .perform(get("/point/{id}", id))
            .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id))
        .andExpect(jsonPath("$.point").value(0));
    verify(pointService).point(id);
  }

  @Test
  void history() throws Exception {
    //given
    long id= 1L;
    //when
    List<PointHistoryDTO> pointHistoryDTO =List.of( new PointHistoryDTO( 1L, 1L, TransactionType.CHARGE, 1L)
        , new PointHistoryDTO(1L, 1L, TransactionType.CHARGE,  1L));
    when(pointService.history(id)).thenReturn(pointHistoryDTO);

    //then
    mockMvc
        .perform(get("/point/{id}/histories", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
    verify(pointService).history(id);
  }

  @Test
  void charge() throws Exception {
    // given
    long userId = 10L;
    long amount = 100L;
    // when
    UserPointDTO userPoint = new UserPointDTO(userId, amount);
    when(pointService.charge(userPoint)).thenReturn(userPoint);

    //then
    mockMvc
        .perform(
            patch("/point/{id}/charge", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userPoint)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId))
        .andExpect(jsonPath("$.point").value(amount));
    verify(pointService).charge(userPoint);
  }

  @Test
  void use() throws Exception {
    // given
    long userId = 10L;
    long amount = 100L;
    // when
    UserPointDTO userPoint = new UserPointDTO(userId, amount);
    when(pointService.use(userPoint)).thenReturn(userPoint);

    //then
    mockMvc
        .perform(
            patch("/point/{id}/use", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userPoint)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId))
        .andExpect(jsonPath("$.point").value(amount));
    verify(pointService).use(userPoint);
  }

}