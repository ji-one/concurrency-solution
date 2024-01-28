package com.example.stock.transaction;

import com.example.stock.service.StockService;

public class TransactionStockService {

    private StockService stockService;

    public TransactionStockService(StockService stockService) {
        this.stockService = stockService;
    }

    public void decrease(Long id, Long quantity) {
        startTransaction();

        stockService.decrease(id, quantity);

        endTransaction(); // 트랜잭션 종료 시점에 실제 데이터베이스에 업데이트
        // 실제 데이터베이스가 업데이트 되기 전에 다른 쓰레드가 decrease 메소드 호출 가능
        // 다른 쓰레드는 갱신되기 전의 값을 가져가서 문제 발생
    }

    private void startTransaction() {
        System.out.println("Transaction Start");
    }

    private void endTransaction() {
        System.out.println("Commit");
    }
}
