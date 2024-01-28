package com.example.stock.facade;

import static org.junit.jupiter.api.Assertions.*;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OptimisticLockStockFacadeTest {

    @Autowired
    private OptimisticLockStockFacade optimisticLockStockFacade;
    // OptimisticLock의 장점: Lock을 통해 Update를 제어하지 않기 때문에 PessimisticLock 보다 성능이 좋음
    // OptimisticLock의 단점: Update가 실패했을 때, 재시도 로직을 개발자가 직접 작성해줘야하는 번거로움이 존재
    // 충돌이 빈번하게 일어나거나, 빈번하게 일어날 것으로 예상된다면 PessimisticLock을 사용하는 것이 좋음
    // 빈번하게 일어나지 않을 것이라고 예상된다면 OptimisticLock을 사용하는 것이 좋음




    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    public void before() {
        stockRepository.saveAndFlush(new Stock(1L, 100L));
    }

    @AfterEach
    public void after() {
        stockRepository.deleteAll();
    }

    @Test
    void 동시에_100개의_요청() throws InterruptedException {
        int threadCount = 100;

        // ExecutorService: 비동기로 실행하는 작업을 단순화하여 사용할 수 있도록 도와주는 Java API
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        // 100개의 요청이 모두 끝나기를 대기
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 100개의 요청
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    optimisticLockStockFacade.decrease(1L, 1L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Stock stock = stockRepository.findById(1L).orElseThrow();
        assertEquals(0L, stock.getQuantity());
    }
}
