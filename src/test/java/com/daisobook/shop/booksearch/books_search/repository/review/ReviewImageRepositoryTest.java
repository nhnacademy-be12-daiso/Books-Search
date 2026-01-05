package com.daisobook.shop.booksearch.books_search.repository.review;

import com.daisobook.shop.booksearch.books_search.entity.review.Review;
import com.daisobook.shop.booksearch.books_search.entity.review.ReviewImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReviewImageRepositoryTest {

    @Autowired
    private ReviewImageRepository reviewImageRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Review savedReview;

    @BeforeEach
    void setUp() {
        // 1. Review 이미지의 부모가 될 Review 객체 생성 및 저장
        // (Review 엔티티의 생성자 구조에 맞게 수정하세요)
        Review review = new Review();
        review.setContent("정말 좋은 책입니다.");
        review.setRating(5);
        this.savedReview = entityManager.persist(review);

        // 2. 해당 리뷰에 대한 이미지들 저장
        ReviewImage img1 = new ReviewImage();
        img1.setReview(savedReview);
        img1.setPath("/images/review1_1.jpg");
        entityManager.persist(img1);

        ReviewImage img2 = new ReviewImage();
        img2.setReview(savedReview);
        img2.setPath("/images/review1_2.jpg");
        entityManager.persist(img2);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("리뷰 ID로 해당 리뷰에 등록된 모든 이미지 목록을 조회한다")
    void findReviewImagesByReview_IdTest() {
        // When
        List<ReviewImage> results = reviewImageRepository.findReviewImagesByReview_Id(savedReview.getId());

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(ReviewImage::getPath)
                           .containsExactlyInAnyOrder("/images/review1_1.jpg", "/images/review1_2.jpg");
        
        // 연관관계 주인 확인
        assertThat(results.getFirst().getReview().getId()).isEqualTo(savedReview.getId());
    }

    @Test
    @DisplayName("이미지가 없는 리뷰 ID로 조회 시 빈 리스트를 반환한다")
    void findReviewImagesByReview_Id_Empty_Test() {
        // When
        List<ReviewImage> results = reviewImageRepository.findReviewImagesByReview_Id(999L);

        // Then
        assertThat(results).isEmpty();
    }
}