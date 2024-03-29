package com.example.stock.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
class StockServiceTest {

    @Autowired
    private PessimisticLockStockService stockService;
    // PessimisticLock의 장점:
    // 1. 충돌이 빈번하게 일어난다면 OptimisticLock보다 성능이 좋음
    // 2. Lock을 통해 Update를 제어하기 때문에 데이터 정합성을 보장할 수 있음
    // PessimisticLock의 단점: Lock을 통해 Update를 제어하기 때문에 성능이 떨어짐

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

    // Q. 왜 synchronized 시 재고감소, 동시에_100개의_요청 테스트 케이스 동시에 통과 X?
    @Test
    void 재고감소() {
        stockService.decrease(1L, 1L);

        Stock stock = stockRepository.findById(1L).orElseThrow();

        assertEquals(99L, stock.getQuantity());
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
                    stockService.decrease(1L, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Stock stock = stockRepository.findById(1L).orElseThrow();
        assertEquals(0L, stock.getQuantity()); // False
        // --> Race Condition: 둘 이상의 쓰레드가 공유 데이터에 접근할 수 있고, 동시에 변경하려고 할 때 발생하는 문제
        // --> 해결 방법: 데이터에 1개의 쓰레드만 접근 가능하도록 제한
    }
}
