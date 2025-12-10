package com.daisobook.shop.booksearch.BooksSearch.repository.review;

import com.daisobook.shop.booksearch.BooksSearch.entity.review.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    List<ReviewImage> findReviewImagesByReview_Id(long reviewId);
}
