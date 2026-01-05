package com.daisobook.shop.booksearch.search.component;

import com.daisobook.shop.booksearch.entity.book.Book;
import com.daisobook.shop.booksearch.search.domain.RabbitBook;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(classes = BookSearchSyncPublisherTest.TestConfig.class)
class BookSearchSyncPublisherTest {

    @Configuration
    static class TestConfig {
        @Bean
        BookSearchSyncPublisher bookSearchSyncPublisher(BookSearchTaskPublisher taskPublisher,
                                                       BookSearchPayloadMapper payloadMapper) {
            return new BookSearchSyncPublisher(taskPublisher, payloadMapper);
        }
    }

    @MockitoBean
    BookSearchTaskPublisher taskPublisher;

    @MockitoBean
    BookSearchPayloadMapper payloadMapper;

    @Resource
    BookSearchSyncPublisher syncPublisher;

    @AfterEach
    void tearDownTxSync() {
        // 테스트 간 TransactionSynchronizationManager 상태 누수 방지
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Nested
    @DisplayName("트랜잭션 활성화 시 동작 테스트")
    class WhenTransactionIsActive {

        @Test
        @DisplayName("publishDeleteAfterCommit: registerSynchronization 되고, 커밋 후에만 실행된다")
        void publishDeleteAfterCommit_shouldRegisterSynchronization_andRunAfterCommitOnly() {
            // given
            TransactionSynchronizationManager.initSynchronization();
            TransactionSynchronizationManager.setActualTransactionActive(true);

            // when
            syncPublisher.publishDeleteAfterCommit("ISBN-DEL", "REASON");

            // then: 아직 커밋 전이므로 실행되면 안 됨
            verify(taskPublisher, never())
                    .publishBookDelete(anyString(), anyString());

            List<TransactionSynchronization> syncs = TransactionSynchronizationManager.getSynchronizations();
            assertThat(syncs)
                    .as("트랜잭션 활성 시 registerSynchronization 되어야 함")
                    .isNotEmpty();

            // 커밋 발생을 수동으로 트리거
            syncs.forEach(TransactionSynchronization::afterCommit);

            verify(taskPublisher)
                    .publishBookDelete("ISBN-DEL", "REASON");
        }

        @Test
        @DisplayName("publishUpsertAfterCommit: registerSynchronization 되고, 커밋 후에만 실행된다")
        void publishUpsertAfterCommit_shouldRegisterSynchronization_andRunAfterCommitOnly() {
            // given
            TransactionSynchronizationManager.initSynchronization();
            TransactionSynchronizationManager.setActualTransactionActive(true);

            RabbitBook rb = new RabbitBook();
            rb.setId(1L);
            rb.setIsbn("ISBN-UP");
            given(payloadMapper.toRabbitBook(any(Book.class))).willReturn(rb);

            // when
            syncPublisher.publishUpsertAfterCommit(new Book(), "UPSERT_REASON");

            // then: 커밋 전에는 업서트 발행 안 함
            verify(taskPublisher, never())
                    .publishBookUpsert(any(RabbitBook.class), anyString());

            List<TransactionSynchronization> syncs = TransactionSynchronizationManager.getSynchronizations();
            assertThat(syncs)
                    .as("트랜잭션 활성 시 동기화가 등록되어야 함")
                    .isNotEmpty();

            syncs.forEach(TransactionSynchronization::afterCommit);

            verify(taskPublisher)
                    .publishBookUpsert(rb, "UPSERT_REASON");
        }
    }

    @Nested
    @DisplayName("트랜잭션 비활성화 시 동작 테스트")
    class WhenNoTransaction {

        @Test
        @DisplayName("publishDeleteAfterCommit: 즉시 실행된다")
        void publishDeleteAfterCommit_shouldRunImmediately() {
            // given: 동기화/트랜잭션 비활성 상태

            // when
            syncPublisher.publishDeleteAfterCommit("ISBN", "R");

            // then
            verify(taskPublisher)
                    .publishBookDelete("ISBN", "R");
        }

        @Test
        @DisplayName("publishUpsertAfterCommit: 즉시 실행된다")
        void publishUpsertAfterCommit_shouldRunImmediately() {
            // given
            RabbitBook rb = new RabbitBook();
            rb.setIsbn("ISBN");
            given(payloadMapper.toRabbitBook(any(Book.class))).willReturn(rb);

            // when
            syncPublisher.publishUpsertAfterCommit(new Book(), "R");

            // then
            verify(taskPublisher)
                    .publishBookUpsert(rb, "R");
        }
    }
}
