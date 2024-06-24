package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.dto.UserPointDTO;
import io.hhplus.tdd.point.repository.PointRepository;
import io.hhplus.tdd.point.repository.PointRepositoryImpl;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UseImplTest {
    private final PointRepository pointRepository = new PointRepositoryImpl(new PointHistoryTable(), new UserPointTable());
    private final QueueManager queueManager = new QueueManager(pointRepository);
    private final UseImpl useImpl = new UseImpl(queueManager);

    @Test
    @DisplayName("사용 메소드가 정상적으로 저장되는지 확인합니다.")
    void useProcess_and_verify_amounts()
        throws ExecutionException, InterruptedException, TimeoutException {
        // given
        long id = 1;
        long amount = 100;
        UserPointDTO userPoint = new UserPointDTO(id, amount);
        // when
        pointRepository.insertOrUpdate(id, amount);
        CompletableFuture<UserPointDTO> future = useImpl.useProcess(userPoint);
        queueManager.processQueue();
        future.get(10, TimeUnit.SECONDS); // 5초 동안 대기
        // then
        var resultPoint = pointRepository.getById(id);
        assertEquals(resultPoint.point(), 0);
    }
}
