
package io.hhplus.tdd.point;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 1. ParamerizedTest를 사용하여 임의의 Long 값을 입력받아 테스트했습니다. (각 테스트당 100회씩)
 * - 의문점: 100회를 테스트를 한것은 임의긴하지만, Long으로 단순 랜덤값을 넣어서 테스트를 한것이 무의미하다고 생각들었습니다.
 * - 개선방안: Dto를 사용할 경우 Dto에 대한 테스트를 진행하거나, 특정 범위의 값을 넣어서 테스트를 진행하는것이 더 의미있는 테스트가 될것이라 생각됩니다.
 * 2. MockMvc를 사용하여, pointController에서 예상되는 결과값이 출력되는지 확인했습니다.
 * 3. verify를 사용하여, pointService에서 해당 메소드가 호출되었는지 확인했습니다.
 */
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

