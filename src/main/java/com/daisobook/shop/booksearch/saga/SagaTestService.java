package com.daisobook.shop.booksearch.saga;

import com.daisobook.shop.booksearch.exception.custom.saga.BookOutOfStockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 재고 부족 시나리오를 위한 테스트 서비스
 */
@Slf4j
@Service
public class SagaTestService {
    public void process() {
        throw new BookOutOfStockException("재고 부족!");
    }
}
