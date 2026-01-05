package com.daisobook.shop.booksearch.BooksSearch.saga;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.saga.BookOutOfStockException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SagaTestServiceTest {

    private final SagaTestService sagaTestService = new SagaTestService();

    @Test
    @DisplayName("process 호출 시 BookOutOfStockException이 발생해야 한다")
    void process_throwsBookOutOfStockException() {
        // [AssertJ 방식] 문장형으로 읽혀서 가장 권장됩니다.
        assertThatThrownBy(sagaTestService::process)
                .isInstanceOf(BookOutOfStockException.class)
                .hasMessageContaining("재고 부족!");
    }

    @Test
    @DisplayName("JUnit5 내장 방식으로 예외 발생 확인")
    void process_throwsException_junit5() {
        // [JUnit5 방식] 발생한 예외 객체를 받아 추가 검증이 가능합니다.
        BookOutOfStockException exception = assertThrows(BookOutOfStockException.class, sagaTestService::process);

        org.junit.jupiter.api.Assertions.assertEquals("재고 부족!", exception.getMessage());
    }
}