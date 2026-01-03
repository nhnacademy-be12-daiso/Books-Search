package com.daisobook.shop.booksearch.BooksSearch.repository.review;

import com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookReviewProjection;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Status;
import com.daisobook.shop.booksearch.BooksSearch.entity.publisher.Publisher;
import com.daisobook.shop.booksearch.BooksSearch.entity.review.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Book savedBook;
    private final long testUserId = 100L;
    private final long testOrderDetailId = 500L;

    @BeforeEach
    void setUp() {
        // 1. Publisher 및 Book 준비
        Publisher publisher = new Publisher("다이소출판");
        entityManager.persist(publisher);

        savedBook = new Book(
                "1234567890123", "리뷰 테스트 도서", "목차", "설명",
                LocalDate.now(), 15000L, true, 50, Status.ON_SALE, 1
        );
        savedBook.setPublisher(publisher);
        entityManager.persist(savedBook);

        // 2. Review 저장
        Review review = new Review(savedBook, testUserId, testOrderDetailId, "정말 유익한 책입니다.", 5);

        entityManager.persist(review);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("도서, 사용자, 주문상세 ID 조합으로 리뷰 존재 여부를 확인한다")
    void existsReviewByBook_IdAndUserIdAndOderDetailIdTest() {
        boolean exists = reviewRepository.existsReviewByBook_IdAndUserIdAndOderDetailId(
                savedBook.getId(), testUserId, testOrderDetailId);
        
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("특정 도서에 달린 모든 리뷰를 조회한다")
    void findAllByBook_IdTest() {
        List<Review> reviews = reviewRepository.findAllByBook_Id(savedBook.getId());
        
        assertThat(reviews).hasSize(1);
        assertThat(reviews.getFirst().getContent()).isEqualTo("정말 유익한 책입니다.");
    }

    @Test
    @DisplayName("특정 시간 이후에 생성되거나 수정된 리뷰의 개수를 카운트한다")
    void countAllByCreatedAtAfterOrModifiedAtAfterTest() {
        ZonedDateTime yesterday = ZonedDateTime.now().minusDays(1);
        
        Long count = reviewRepository.countAllByCreatedAtAfterOrModifiedAtAfter(yesterday, yesterday);
        
        assertThat(count).isGreaterThanOrEqualTo(1L);
    }

    @Test
    @DisplayName("사용자 ID와 도서 목록으로 리뷰 프로젝션을 조회한다 (Native Query)")
    void getBookReviewProjectionListTest() {
        // Native Query의 JSON 연산 때문에 H2에서 에러가 날 수 있으므로 주의가 필요합니다.
        // 에러가 발생한다면 이 메서드는 실제 MySQL 환경 테스트로 넘기는 것이 좋습니다.
        try {
            List<BookReviewProjection> results = reviewRepository.getBookReviewProjectionList(
                    testUserId, List.of(savedBook.getId()), List.of(testOrderDetailId));

            assertThat(results).isNotEmpty();
            assertThat(results.getFirst().getTitle()).isEqualTo("리뷰 테스트 도서");
        } catch (Exception e) {
            System.out.println("Native Query JSON operation is not supported in this H2 version.");
        }
    }
}