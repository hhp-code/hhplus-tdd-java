package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.repository.PointRepository;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPointDTO;
import io.hhplus.tdd.point.service.charge.ChargeImpl;
import io.hhplus.tdd.point.service.charge.ChargeSpecification;
import io.hhplus.tdd.point.service.use.UseImpl;
import io.hhplus.tdd.point.service.use.UseSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.*;

@Slf4j
@Component
public class QueueManager  {
    private final PointRepository pointRepository;
    private final ChargeSpecification chargeImpl;
    private final UseSpecification useImpl;

    // 요청을 처리하기 위한 ConcurrentLinkedQueue
    private final Queue<QueueEntity> requestQueue = new ConcurrentLinkedQueue<>();
    // 비동기 처리를 위한 CompletableFuture
    private final Map<Long, CompletableFuture<UserPointDTO>> futureMap = new ConcurrentHashMap<>();
    // 비동기 대기를 위한 타임아웃 설정
    private final Duration TIMEOUT = Duration.ofSeconds(5);

    public QueueManager(PointRepository pointRepository, ChargeImpl chargeImpl, UseImpl useImpl) {
        this.pointRepository = pointRepository;
        this.chargeImpl = chargeImpl;
        this.useImpl = useImpl;
    }

    UserPointDTO handleRequest(Callable<CompletableFuture<UserPointDTO>> request) {
        try {
            CompletableFuture<UserPointDTO> future = request.call();
            return future.get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            PointService.errorMessageThrowing("서버 에러가 발생했습니다.");
        } catch (TimeoutException e) {
            PointService.errorMessageThrowing("처리 시간이 초과되었습니다.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public CompletableFuture<UserPointDTO> addToQueue(long id, long amount, TransactionType transactionType) {
        CompletableFuture<UserPointDTO> future = new CompletableFuture<>();
        requestQueue.offer(new QueueEntity(id, amount, transactionType));
        futureMap.put(id, future);
        return future;
    }

    @Scheduled(fixedDelay = 1000)
    public void processQueue() {
        while (!requestQueue.isEmpty()) {
            QueueEntity request = requestQueue.poll();
            if (request != null) {
                processRequest(request);
            }
        }
    }

    private void processRequest(QueueEntity request) {
        if (request.transactionType == TransactionType.CHARGE) {
            chargeImpl.chargeProcess(request.id, request.amount);
        } else if (request.transactionType == TransactionType.USE) {
            useImpl.useProcess(request.id, request.amount);
        }
        Optional<UserPointDTO> result = pointRepository.selectById(request.id);
        if (result.isPresent()) {
            futureMap.get(request.id).complete(result.get());
        } else {
            futureMap.get(request.id).completeExceptionally(new IllegalArgumentException("UserPointDTO is not found"));
        }
    }
}
