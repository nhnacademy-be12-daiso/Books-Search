package com.daisobook.shop.booksearch.BooksSearch.repository.review;

import com.daisobook.shop.booksearch.BooksSearch.entity.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsReviewByBook_IdAndUserIdAndOderDetailId(long bookId, long userId, long oderDetailId);

    Review findReviewById(long id);

    List<Review> findAllByUserId(long userId);

    List<Review> findAllByBook_Id(long bookId);

    Review findReviewByUserIdAndBook_IdAndOderDetailId(long userId, long bookId, long oderDetailId);
}
