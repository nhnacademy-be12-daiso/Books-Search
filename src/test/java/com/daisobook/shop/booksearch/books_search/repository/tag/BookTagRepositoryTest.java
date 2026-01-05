package com.daisobook.shop.booksearch.books_search.repository.tag;

import com.daisobook.shop.booksearch.books_search.entity.book.Book;
import com.daisobook.shop.booksearch.books_search.entity.book.Status;
import com.daisobook.shop.booksearch.books_search.entity.publisher.Publisher;
import com.daisobook.shop.booksearch.books_search.entity.tag.BookTag;
import com.daisobook.shop.booksearch.books_search.entity.tag.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookTagRepositoryTest {

    @Autowired
    private BookTagRepository bookTagRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Book savedBook;
    private Tag tag1;
    private Tag tag2;

    @BeforeEach
    void setUp() {
        // 1. 기초 데이터(출판사, 도서) 준비
        Publisher publisher = new Publisher("다이소출판");
        entityManager.persist(publisher);

        savedBook = new Book(
                "1234567890123", "태그 테스트 도서", "목차", "설명",
                LocalDate.now(), 15000L, true, 50, Status.ON_SALE, 1
        );
        savedBook.setPublisher(publisher);
        entityManager.persist(savedBook);

        // 2. 태그 데이터 준비
        tag1 = new Tag("베스트셀러");
        tag2 = new Tag("추천도서");
        Tag tag3 = new Tag("이달의도서");
        entityManager.persist(tag1);
        entityManager.persist(tag2);
        entityManager.persist(tag3);

        // 3. 도서와 태그 연결 (BookTag)
        BookTag bookTag1 = new BookTag();
        bookTag1.setBook(savedBook);
        bookTag1.setTag(tag1);
        entityManager.persist(bookTag1);

        BookTag bookTag2 = new BookTag();
        bookTag2.setBook(savedBook);
        bookTag2.setTag(tag2);
        entityManager.persist(bookTag2);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("도서 ID와 여러 태그 ID 목록을 이용해 일치하는 BookTag 목록을 조회한다")
    void findAllByBook_IdAndTag_IdInTest() {
        // Given
        List<Long> tagIds = List.of(tag1.getId(), tag2.getId());

        // When
        List<BookTag> results = bookTagRepository.findAllByBook_IdAndTag_IdIn(savedBook.getId(), tagIds);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(bt -> bt.getTag().getName())
                           .containsExactlyInAnyOrder("베스트셀러", "추천도서");
    }

    @Test
    @DisplayName("도서에 연결되지 않은 태그 ID로 조회 시 빈 리스트를 반환한다")
    void findAllByBook_IdAndTag_IdIn_Empty_Test() {
        // Given
        List<Long> invalidTagIds = List.of(999L, 1000L);

        // When
        List<BookTag> results = bookTagRepository.findAllByBook_IdAndTag_IdIn(savedBook.getId(), invalidTagIds);

        // Then
        assertThat(results).isEmpty();
    }
}