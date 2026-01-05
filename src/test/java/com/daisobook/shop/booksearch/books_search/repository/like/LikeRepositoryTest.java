package com.daisobook.shop.booksearch.books_search.repository.like;

import com.daisobook.shop.booksearch.books_search.dto.projection.BookIdProjection;
import com.daisobook.shop.booksearch.books_search.entity.book.Book;
import com.daisobook.shop.booksearch.books_search.entity.book.Status;
import com.daisobook.shop.booksearch.books_search.entity.like.Like;
import com.daisobook.shop.booksearch.books_search.entity.publisher.Publisher;
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
class LikeRepositoryTest {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Book savedBook;
    private final long testUserId = 100L;

    @BeforeEach
    void setUp() {
        // 1. Publisher 저장
        Publisher publisher = new Publisher("다이소출판");
        entityManager.persist(publisher);

        // 2. Book 저장
        Book book = new Book(
                "1234567890123", "좋아요 테스트 도서", "목차", "설명",
                LocalDate.now(), 20000L, true, 100, Status.ON_SALE, 1
        );
        book.setPublisher(publisher);
        this.savedBook = entityManager.persist(book);

        // 3. Like 저장 (userId를 직접 세팅한다고 가정)
        // 엔티티 구조에 따라 생성자나 setter를 조정하세요.
        Like like = new Like(book, testUserId);
        
        entityManager.persist(like);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("도서 ID와 사용자 ID로 좋아요 존재 여부를 확인한다")
    void existsLikeByBookIdAndUserIdTest() {
        boolean exists = likeRepository.existsLikeByBookIdAndUserId(savedBook.getId(), testUserId);
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("사용자 ID로 좋아요한 모든 목록을 조회한다")
    void findAllByUserIdTest() {
        List<Like> likes = likeRepository.findAllByUserId(testUserId);
        assertThat(likes).hasSize(1);
        assertThat(likes.getFirst().getBook().getId()).isEqualTo(savedBook.getId());
    }

    @Test
    @DisplayName("도서별 좋아요 총 개수를 카운트한다")
    void countAllByBook_IdTest() {
        int count = likeRepository.countAllByBook_Id(savedBook.getId());
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("특정 사용자가 여러 도서 중 좋아요한 도서 ID들만 조회한다 (Native Query)")
    void getLikeByUserIdAndBookIdInTest() {
        // When
        List<BookIdProjection> results = likeRepository.getLikeByUserIdAndBookIdIn(
                testUserId, List.of(savedBook.getId(), 999L)
        );

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getId()).isEqualTo(savedBook.getId());
    }

    @Test
    @DisplayName("도서 엔티티와 사용자 ID를 사용하여 좋아요를 삭제한다")
    void deleteLikeByBookAndUserIdTest() {
        // When
        // 영속성 컨텍스트에 있는 book을 가져옴
        Book book = entityManager.find(Book.class, savedBook.getId());
        likeRepository.deleteLikeByBookAndUserId(book, testUserId);
        entityManager.flush();
        entityManager.clear();

        // Then
        boolean exists = likeRepository.existsLikeByBookIdAndUserId(savedBook.getId(), testUserId);
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("도서 ID, 좋아요 ID, 사용자 ID를 모두 만족하는 좋아요를 찾는다")
    void findLikeByBook_IdAndIdAndUserIdTest() {
        // Given - 실제 생성된 좋아요 ID 확인을 위해 먼저 조회
        Like existingLike = likeRepository.findAllByUserId(testUserId).getFirst();

        // When
        Like found = likeRepository.findLikeByBook_IdAndIdAndUserId(
                savedBook.getId(), existingLike.getId(), testUserId
        );

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(existingLike.getId());
    }
}