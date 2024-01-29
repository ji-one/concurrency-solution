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
class NamedLockStockFacadeTest {

    // NamedLock은 주로 분산 Lock을 구현할 때 사용
    // PessimisticLock은 timeout을 구현하기 힘들지만, NamedLock은 timeout을 구현하기 쉬움
    // Data 삽입 시에 정합성을 맞춰야하는 경우에도 NamedLock을 사용할 수 있음
    // 하지만 transaction 종료 시에 Lock 해제 세션 관리를 잘 해줘야하고 실제로 사용 시에는 구현 방법이 복잡할 수 있음.

    @Autowired
    private NamedLockStockFacade namedLockStockFacade;

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
                    namedLockStockFacade.decrease(1L, 1L);
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
