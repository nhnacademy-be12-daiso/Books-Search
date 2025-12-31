package com.daisobook.shop.booksearch.BooksSearch.repository.book;

import com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookDetailProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookIdProjection;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Status;
import com.daisobook.shop.booksearch.BooksSearch.entity.publisher.Publisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

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

    @BeforeEach
    void setUp() {
        // 1. 연관 엔티티 저장 (Publisher)
        Publisher publisher = new Publisher("다이소출판");
        entityManager.persist(publisher);

        // 2. 도서 엔티티 생성 (ID는 Auto Increment 전략에 따라 비워둠)
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
        book.setPublisher(publisher);

        // 3. DB 반영 및 영속성 컨텍스트 초기화
        // persistAndFlush를 통해 DB에서 생성된 ID를 즉시 가져옵니다.
        this.savedBook = entityManager.persistAndFlush(book);
        entityManager.clear();
    }

    @Test
    @DisplayName("ISBN으로 조회를 시도하여, 생성된 Auto Increment ID(long)를 가져온다")
    void getBookIdTest() {
        // Given
        String targetIsbn = "1234567890123";

        // When
        // @Query에 'AS id'가 추가된 리포지토리 메서드 호출
        BookIdProjection result = bookRepository.getBookId(targetIsbn);

        // Then
        assertThat(result).withFailMessage("조회 결과가 null입니다. 별칭(AS id) 매핑을 확인하세요.").isNotNull();

        long actualIdFromDb = result.getId(); // 이제 null 에러 없이 long에 잘 담깁니다.
        long expectedId = savedBook.getId();

        assertThat(actualIdFromDb).isEqualTo(expectedId);
    }

//    @Test
//    @DisplayName("생성된 PK(long)를 사용하여 상세 정보를 조회한다")
//    void getBookDetailByIdTest() {
//        // When
//        BookDetailProjection detail = bookRepository.getBookDetailById(savedBook.getId(), false);
//
//        // Then
//        assertThat(detail).isNotNull();
//        assertThat(detail.getId()).isEqualTo(savedBook.getId());
//        assertThat(detail.getTitle()).isEqualTo("스프링 부트 테스트");
//        assertThat(detail.getIsbn()).isEqualTo("1234567890123");
//    }

    @Test
    @DisplayName("상태별 도서 수량을 카운트한다")
    void countAllByStatusTest() {
        // When
        Long count = bookRepository.countAllByStatus(Status.ON_SALE);

        // Then
        assertThat(count).isEqualTo(1L);
    }
}