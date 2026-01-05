package com.daisobook.shop.booksearch.repository.saga;

import com.daisobook.shop.booksearch.entity.saga.BookOutbox;
import com.daisobook.shop.booksearch.saga.SagaTopic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookOutboxRepositoryTest {

    @Autowired
    private BookOutboxRepository bookOutboxRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("아웃박스 메시지를 저장하고 ID로 다시 조회한다")
    void saveAndFindByIdTest() {
        // Given
        BookOutbox outbox = new BookOutbox(1L, "BOOK", SagaTopic.ORDER_EXCHANGE,
                "testKey","{\"id\": 1, \"title\": \"테스트 도서\"}");

        // When
        BookOutbox savedOutbox = bookOutboxRepository.save(outbox);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<BookOutbox> foundOutbox = bookOutboxRepository.findById(savedOutbox.getId());
        assertThat(foundOutbox).isPresent();
        assertThat(foundOutbox.get().getAggregateType()).isEqualTo("BOOK");
        assertThat(foundOutbox.get().getPayload()).contains("테스트 도서");
    }
}