package com.daisobook.shop.booksearch.books_search.repository.author;

import com.daisobook.shop.booksearch.books_search.entity.author.Author;
import com.daisobook.shop.booksearch.books_search.entity.author.BookAuthor;
import com.daisobook.shop.booksearch.books_search.entity.author.Role;
import com.daisobook.shop.booksearch.books_search.entity.book.Book;
import com.daisobook.shop.booksearch.books_search.entity.book.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookAuthorRepositoryTest {

    @Autowired
    private BookAuthorRepository bookAuthorRepository;
    
    @Autowired
    private TestEntityManager entityManager; // 연관관계 저장을 위해 사용

    @Test
    @DisplayName("BookAuthor 엔티티를 저장하고 조회한다")
    void saveAndFindTest() {
        // 1. 기초 데이터 준비 (영속화)
        Author author = entityManager.persist(new Author("조영호"));
        Role role = entityManager.persist(new Role("AUTHOR"));
        // Book 엔티티 생성 시 필요한 필드(제목, ISBN 등)에 맞춰 생성하세요.
        // 2. 도서 데이터 준비 (제시해주신 생성자 기준)
        Book book = new Book(
                "9788998139766",           // isbn
                "객체지향의 사실과 오해",       // title
                "목차 내용...",              // index
                "객체지향이란 무엇인가",       // description
                LocalDate.of(2015, 6, 17), // publicationDate
                20000L,                    // price
                true,                      // isPackaging
                100,                       // stock
                Status.ON_SALE,          // status (프로젝트의 Enum 상수를 입력하세요)
                1                          // volumeNo
        );
        entityManager.persist(book);

        // 2. 연결 엔티티 생성 및 저장
        BookAuthor bookAuthor = new BookAuthor(book, author);
        bookAuthor.setRole(role);
        BookAuthor saved = bookAuthorRepository.save(bookAuthor);

        // 3. 검증
        entityManager.flush();
        entityManager.clear(); // 1차 캐시를 비워 DB에서 새로 조회하도록 유도

        BookAuthor found = bookAuthorRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getAuthor().getName()).isEqualTo("조영호");
        assertThat(found.getRole().getName()).isEqualTo("AUTHOR");
        assertThat(found.getBook().getTitle()).isEqualTo("객체지향의 사실과 오해");
    }
}