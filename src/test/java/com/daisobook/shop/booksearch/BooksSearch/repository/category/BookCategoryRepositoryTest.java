package com.daisobook.shop.booksearch.BooksSearch.repository.category;

import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Status;
import com.daisobook.shop.booksearch.BooksSearch.entity.category.BookCategory;
import com.daisobook.shop.booksearch.BooksSearch.entity.category.Category;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookCategoryRepositoryTest {

    @Autowired
    private BookCategoryRepository bookCategoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Book savedBook;
    private Category savedCategory;
    private BookCategory savedBookCategory;

    @BeforeEach
    void setUp() {
        // 1. Publisher 생성 및 저장
        Publisher publisher = new Publisher("다이소출판");
        entityManager.persist(publisher);

        // 2. Book 생성 및 저장
        Book book = new Book("1234567890", "테스트 도서", "목차", "설명", 
                             LocalDate.now(), 10000L, true, 10, Status.ON_SALE, 1);
        book.setPublisher(publisher);
        this.savedBook = entityManager.persist(book);

        // 3. Category 생성 및 저장
        Category category = new Category(1,"IT/컴퓨터", 1); // 생성자 파라미터는 엔티티 구조에 맞게 수정
        this.savedCategory = entityManager.persist(category);

        // 4. BookCategory(중간 엔티티) 연결 및 저장
        BookCategory bookCategory = new BookCategory();
        bookCategory.setBook(savedBook);
        bookCategory.setCategory(savedCategory);
        this.savedBookCategory = entityManager.persistAndFlush(bookCategory);

        entityManager.clear();
    }

    @Test
    @DisplayName("도서 ID로 해당 도서의 모든 카테고리 관계를 조회한다")
    void findAllByBook_IdTest() {
        // When
        List<BookCategory> results = bookCategoryRepository.findAllByBook_Id(savedBook.getId());

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getBook().getId()).isEqualTo(savedBook.getId());
        assertThat(results.getFirst().getCategory().getName()).isEqualTo("IT/컴퓨터");
    }

    @Test
    @DisplayName("카테고리 ID로 도서와 카테고리를 페치 조인하여 조회한다")
    void findAllByCategoryIdWithBookTest() {
        // When
        // 리포지토리 메서드명이 findAllByCategoryIdWithBook 임을 확인
        List<BookCategory> results = bookCategoryRepository.findAllByCategoryIdWithBook(savedCategory.getId());

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results.getFirst().getCategory().getId()).isEqualTo(savedCategory.getId());
        assertThat(results.getFirst().getBook().getTitle()).isEqualTo("테스트 도서");
    }

    @Test
    @DisplayName("여러 ID 목록에 해당하는 데이터를 일괄 삭제한다")
    void removeAllByIdInTest() {
        // Given
        List<Long> idsToDelete = List.of(savedBookCategory.getId());

        // When
        bookCategoryRepository.removeAllByIdIn(idsToDelete);
        entityManager.flush(); // 삭제 쿼리 즉시 반영
        entityManager.clear();

        // Then
        List<BookCategory> results = bookCategoryRepository.findAll();
        assertThat(results).isEmpty();
    }
}