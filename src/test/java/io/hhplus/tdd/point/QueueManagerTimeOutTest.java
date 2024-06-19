package io.hhplus.tdd.point;

import io.hhplus.tdd.point.dto.UserPointDTO;
import io.hhplus.tdd.point.repository.PointRepository;
import io.hhplus.tdd.point.repository.PointRepositoryImpl;
import io.hhplus.tdd.point.service.charge.ChargeImpl;
import io.hhplus.tdd.point.service.PointService;
import io.hhplus.tdd.point.service.QueueManager;
import io.hhplus.tdd.point.service.history.HistoryImpl;
import io.hhplus.tdd.point.service.point.PointImpl;
import io.hhplus.tdd.point.service.use.UseImpl;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

public class QueueManagerTimeOutTest {
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
    private final UseImpl useImpl = new UseImpl(pointRepository);
    private final ChargeImpl chargeImpl = new ChargeImpl(pointRepository);
    private final PointImpl pointImpl = new PointImpl(pointRepository);
    private final HistoryImpl historyImpl = new HistoryImpl(pointRepository);
    private final QueueManager queueManager = new QueueManager(pointRepository, chargeImpl, useImpl);
    private final PointService pointService = new PointService(queueManager, pointImpl, historyImpl);
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

        queueManager.addToQueue(userId, amount, TransactionType.CHARGE);
        queueManager.processQueue();
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
