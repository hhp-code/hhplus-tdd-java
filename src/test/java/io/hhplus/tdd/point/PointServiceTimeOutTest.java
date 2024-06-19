package io.hhplus.tdd.point;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

public class PointServiceTimeOutTest {
    PointRepository pointRepository = new PointRepositoryImpl(){
        @Override
        public Optional<UserPointDTO> selectById(long id) {
            try{
                Thread.sleep(10000);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            return Optional.of(new UserPointDTO(id, 100));
        }
    };
    private final PointService pointService = new PointService(pointRepository);

    /**
     * 충전 시간이 초과되는 경우 "처리시간이 초과되었습니다." 메시지를 출력하는가?
     */
    @Test
    void charge_fail_by_timeout() {
        // given
        long userId = 1L;
        long amount = 1000L;

        // when & then
        assertTimeoutPreemptively(Duration.ofMillis(6000), () -> {
            assertThatThrownBy(() -> {
                pointService.charge(userId, amount);
                pointRepository.selectById(userId);
            })
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("처리 시간이 초과되었습니다.");
        });
    }

    /**
     * 사용 시간이 초과되는 경우 "처리시간이 초과되었습니다." 메시지를 출력하는가?
     */
    @Test
    void use_fail_by_timeout(){
        // given
        long userId = 1L;
        long amount = 1000L;

        // when
        pointService.addToQueueByCharge(userId, amount+1L);
        pointService.queueOperation();
        // then
        assertTimeoutPreemptively(Duration.ofMillis(10000), () -> {
            assertThatThrownBy(() -> {
                pointService.use(userId, amount);
                pointRepository.selectById(userId);
            })
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("처리 시간이 초과되었습니다.");
        });
    }
}
