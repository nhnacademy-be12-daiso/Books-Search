package com.daisobook.shop.booksearch.repository.book;

import com.daisobook.shop.booksearch.dto.projection.BookAdminProjection;
import com.daisobook.shop.booksearch.dto.projection.BookIdProjection;
import com.daisobook.shop.booksearch.entity.book.Book;
import com.daisobook.shop.booksearch.entity.book.Status;
import com.daisobook.shop.booksearch.entity.publisher.Publisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test") // application-test.yml (H2 MySQL 모드) 사용
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Book savedBook;
    private Publisher savedPublisher;

    @BeforeEach
    void setUp() {
        // 1. 연관 엔티티 저장 (Publisher)
        savedPublisher = new Publisher("다이소출판");
        entityManager.persist(savedPublisher);

        // 2. 도서 엔티티 생성 및 저장
        Book book = new Book(
                "1234567890123",
                "스프링 부트 테스트",
                "목차",
                "설명",
                LocalDate.now(),
                30000L,
                true,
                50,
                Status.ON_SALE,
                1
        );
        book.setPublisher(savedPublisher);

        this.savedBook = entityManager.persistAndFlush(book);
        entityManager.clear(); // 영속성 컨텍스트 비우기 (DB 조회를 명확히 확인)
    }

    @Test
    @DisplayName("ISBN으로 도서 ID(Projection) 조회 테스트")
    void getBookIdTest() {
        // When
        BookIdProjection result = bookRepository.getBookId("1234567890123");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(savedBook.getId());
    }

    //네이티브 쿼리가 테스트하기 힘들다 실제 db에 연결해서 테스트하는게 아니면
//    @Test
//    @DisplayName("도서 ID 목록으로 ISBN 리스트 조회 테스트")
//    void findBooksByIsbnInTest() {
//        // When
//        List<BookIsbnProjection> results = bookRepository.findBooksByIsbnIn(List.of("1234567890123"));
//
//        // Then
//        assertThat(results).hasSize(1);
//        assertThat(results.getFirst().getIsbn()).isEqualTo("1234567890123");
//    }
//
//    @Test
//    @DisplayName("도서 상세 정보 조회 (Native Query) 테스트")
//    void getBookDetailByIdTest() {
//        /*
//         주의: getBookDetailById는 JSON_ARRAYAGG 등 MySQL 전용 함수를 사용합니다.
//         H2 DB 설정에서 'MODE=MySQL'이 설정되어 있어야 하며, 복잡한 JSON 연산은 H2에서 실패할 수 있습니다.
//        */
//        // When
//        BookDetailProjection detail = bookRepository.getBookDetailById(savedBook.getId(), false);
//
//        // Then
//        assertThat(detail).isNotNull();
//        assertThat(detail.getTitle()).isEqualTo("스프링 부트 테스트");
//        assertThat(detail.getIsDeleted()).isFalse();
//    }

    @Test
    @DisplayName("상태별 도서 수량 카운트 테스트")
    void countAllByStatusTest() {
        // When
        Long count = bookRepository.countAllByStatus(Status.ON_SALE);

        // Then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("관리자용 도서 목록 페이징 조회 테스트")
    void getBookAdminProjectionTest() {
        // When
        Page<BookAdminProjection> page = bookRepository.getBookAdminProjection(PageRequest.of(0, 10));

        // Then
        assertThat(page.getContent()).isNotEmpty();
        assertThat(page.getContent().getFirst().getIsbn()).isEqualTo("1234567890123");
    }

    @Test
    @DisplayName("신간 도서 ID 목록 조회 테스트")
    void getBookIdByNewReleasesTest() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);

        // When
        List<BookIdProjection> results = bookRepository.getBookIdByNewReleases(startDate, PageRequest.of(0, 10));

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results.getFirst().getId()).isEqualTo(savedBook.getId());
    }
}