package com.example.stock.service;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

//    @Transactional // @Transactional을 이용하면 해당 클래스를 매핑한 새로운 클레스를 만들어서 실행함 (ex: TransactionalStockService)
    public synchronized void decrease(Long id, Long quantity) { // synchronized: 해당 메소드에 1개의 쓰레드만 접근 가능
        // Stock 조회
        // 재고를 감소시킨 뒤
        // 갱신된 값을 저장하도록 구현
        Stock stock = stockRepository.findById(id).orElseThrow();
        stock.decrease(quantity);

        stockRepository.saveAndFlush(stock);
    }
}
