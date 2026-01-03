package com.daisobook.shop.booksearch.BooksSearch.repository.saga;

import com.daisobook.shop.booksearch.BooksSearch.entity.saga.BookDeduplicationLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookDeduplicationRepositoryTest {

    @Autowired
    private BookDeduplicationRepository bookDeduplicationRepository;

    @Autowired
    private TestEntityManager entityManager;

    private final String existingMessageId = "msg-12345-uuid";

    @BeforeEach
    void setUp() {
        // 테스트용 중복 방지 로그 저장
        BookDeduplicationLog log = new BookDeduplicationLog(existingMessageId);
        // 필요한 다른 필드(예: 처리 일시 등)가 있다면 추가 세팅
        
        entityManager.persist(log);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("이미 존재하는 메시지 ID로 존재 여부를 확인하면 true를 반환한다")
    void existsByMessageId_True_Test() {
        // When
        boolean exists = bookDeduplicationRepository.existsByMessageId(existingMessageId);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 메시지 ID로 존재 여부를 확인하면 false를 반환한다")
    void existsByMessageId_False_Test() {
        // When
        boolean exists = bookDeduplicationRepository.existsByMessageId("new-message-id-999");

        // Then
        assertThat(exists).isFalse();
    }
}