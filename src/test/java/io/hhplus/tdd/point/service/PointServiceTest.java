package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.dto.PointHistoryDTO;
import io.hhplus.tdd.point.dto.UserPointDTO;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 포인트 서비스는 요청을 보내는 역할만 수행하기위해 목을 사용해 테스트를 호출만 검증했습니다.
 */
@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private QueueManager queueManager;

    @Mock
    private PointImpl pointImpl;
    @Mock
    private ChargeImpl chargeImpl;
    @Mock
    private UseImpl useImpl;

    @Mock
    private HistoryImpl historyImpl;

    // * PointService에 대한 테스트를 진행합니다.
    @InjectMocks
    private PointService pointService;

    private UserPointDTO userPointDTO;
    private PointHistoryDTO pointHistoryDTO;
    private CompletableFuture<UserPointDTO> completableFuture;

    @BeforeEach
    void setUp() {
        userPointDTO = new UserPointDTO(1L, 100);
        completableFuture = new CompletableFuture<>();
        completableFuture.complete(userPointDTO);
        pointHistoryDTO = new PointHistoryDTO(1L, 1L, TransactionType.CHARGE, 1L);
    }

    @Test
    @DisplayName("포인트 조회 메소드가 정상적으로 호출되는지 확인합니다.")
    void when_point_called_use_pointImpl() {
        when(pointImpl.point(1L)).thenReturn(userPointDTO);

        UserPointDTO result = pointService.point(1L);

        assertEquals(userPointDTO, result);
        verify(pointImpl, times(1)).point(1L);
    }

    @Test
    @DisplayName("포인트 히스토리 조회 메소드가 정상적으로 호출되는지 확인합니다.")
    void when_history_called_use_historyImpl() {
        List<PointHistoryDTO> historyList = Collections.singletonList(pointHistoryDTO);
        when(historyImpl.history(1L)).thenReturn(historyList);

        List<PointHistoryDTO> result = pointService.history(1L);

        assertEquals(historyList, result);
        verify(historyImpl, times(1)).history(1L);
    }

    @Test
    @DisplayName("충전 메소드가 정상적으로 호출되는지 확인합니다.")
    void when_charge_called_use_chargeImpl() {
        when(chargeImpl.chargeProcess(userPointDTO)).thenReturn(completableFuture);

        UserPointDTO result = pointService.charge(userPointDTO).join();

        assertEquals(completableFuture.join(), result);
        verify(chargeImpl, times(1)).chargeProcess(userPointDTO);
    }

    @Test
    @DisplayName("사용 메소드가 정상적으로 호출되는지 확인합니다.")
    void when_use_called_use_useImpl() {
        when(useImpl.useProcess(userPointDTO)).thenReturn(completableFuture);

        CompletableFuture<UserPointDTO> result = pointService.use(userPointDTO);

        assertEquals(completableFuture, result);
        verify(useImpl, times(1)).useProcess(userPointDTO);
    }

    @Test
    @DisplayName("에러 메시지 출력 및 예외 발생 메소드가 정상적으로 호출되는지 확인합니다.")
    void when_errorThrowing_called() {
        String errorMessage = "Error occurred";

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> PointService.errorMessageThrowing(errorMessage));

        assertEquals(errorMessage, exception.getMessage());
    }
}