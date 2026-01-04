package com.daisobook.shop.booksearch.BooksSearch.config;

import com.daisobook.shop.booksearch.BooksSearch.entity.saga.BookOutbox;
import com.daisobook.shop.booksearch.BooksSearch.entity.saga.OutboxStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class) // @EnableJpaAuditing 설정을 반드시 포함
class BookOutboxAuditingTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Outbox 메시지 저장 시 생성일과 수정일이 자동으로 기록되어야 한다")
    void outbox_auditing_insert_test() {
        // given
        BookOutbox outbox = new BookOutbox(1L, "BOOK", "book-exchange", "book.created", "{\"id\":1}");

        // when
        BookOutbox saved = entityManager.persistAndFlush(outbox);

        // then
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(OutboxStatus.PENDING);
    }

    @Test
    @DisplayName("Outbox 상태 변경 시 updatedAt 필드가 갱신되어야 한다")
    void outbox_auditing_update_test() throws InterruptedException {
        // given
        BookOutbox outbox = entityManager.persistAndFlush(
            new BookOutbox(1L, "BOOK", "book-exchange", "book.created", "{}")
        );
        LocalDateTime firstUpdateTime = outbox.getUpdatedAt();

        // when: 상태 변경 (PENDING -> PUBLISHED)
        outbox.markAsPublished();
        entityManager.flush(); // DB에 반영하여 Auditing 트리거

        // then
        assertThat(outbox.getUpdatedAt()).isAfter(firstUpdateTime);
    }
}