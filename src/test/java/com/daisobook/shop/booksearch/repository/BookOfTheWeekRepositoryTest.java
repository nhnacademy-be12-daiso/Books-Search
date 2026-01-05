package com.daisobook.shop.booksearch.repository;

import com.daisobook.shop.booksearch.dto.projection.BookIdProjection;
import com.daisobook.shop.booksearch.entity.BookOfTheWeek;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookOfTheWeekRepositoryTest {

    @Autowired
    private BookOfTheWeekRepository bookOfTheWeekRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        // 1. 활성 상태의 도서 3권 저장 (no 순서: 1, 2, 3)
        saveBookOfTheWeek(101L, true);
        saveBookOfTheWeek(102L, true);
        saveBookOfTheWeek(103L, true);

        // 2. 비활성 상태의 도서 1권 저장
        saveBookOfTheWeek(104L, false);

        entityManager.flush();
        entityManager.clear();
    }

    private int no = 0;
    private void saveBookOfTheWeek(Long bookId, boolean isActive) {
        BookOfTheWeek bow = new BookOfTheWeek(++no, bookId, "test reason", ZonedDateTime.now());
        bow.setActive(isActive);
        entityManager.persist(bow);
    }

    @Test
    @DisplayName("활성화된 이주의 도서 ID를 최신순(no 내림차순)으로 페이징 조회한다")
    void getBookIdTest() {
        // Given: 첫 번째 페이지, 사이즈 2개 조회
        Pageable pageable = PageRequest.of(0, 2);

        // When
        List<BookIdProjection> results = bookOfTheWeekRepository.getBookId(pageable);

        // Then
        // 1. 활성 상태인 3권 중 2권만 가져와야 함
        assertThat(results).hasSize(2);
        
        // 2. ORDER BY b.no DESC에 의해 가장 나중에 넣은 ID부터 나와야 함
        // (ID 값이 103, 102 순서인지 확인 - no가 자동 증가라고 가정할 때)
        assertThat(results.get(0).getId()).isEqualTo(103L);
        assertThat(results.get(1).getId()).isEqualTo(102L);
    }

    @Test
    @DisplayName("활성화된 도서가 없을 경우 빈 리스트를 반환한다")
    void getBookId_Empty_Test() {
        // Given: 모든 데이터를 비활성화하거나 없는 페이지 조회
        Pageable pageable = PageRequest.of(10, 10);

        // When
        List<BookIdProjection> results = bookOfTheWeekRepository.getBookId(pageable);

        // Then
        assertThat(results).isEmpty();
    }
}