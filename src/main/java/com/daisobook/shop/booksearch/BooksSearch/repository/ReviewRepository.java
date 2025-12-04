package com.daisobook.shop.booksearch.BooksSearch.repository;

import com.daisobook.shop.booksearch.BooksSearch.dto.response.ReviewRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsReviewByBook_IdAndUserIdAndOderDetailId(long bookId, long userId, long oderDetailId);

    Review findReviewById(long id);

    List<Review> findAllByUserId(long userId);

    List<Review> findAllByBook_Id(long bookId);

    Review findReviewByUserIdAndBook_IdAndOderDetailId(long userId, long bookId, long oderDetailId);
}
