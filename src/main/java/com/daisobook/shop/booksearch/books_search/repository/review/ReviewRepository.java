package com.daisobook.shop.booksearch.books_search.repository.review;

import com.daisobook.shop.booksearch.books_search.dto.projection.BookReviewProjection;
import com.daisobook.shop.booksearch.books_search.entity.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsReviewByBook_IdAndUserIdAndOderDetailId(long bookId, long userId, long oderDetailId);

    Review findReviewById(long id);

    List<Review> findAllByUserId(long userId);

    List<Review> findAllByBook_Id(long bookId);

    Review findReviewByUserIdAndBook_IdAndOderDetailId(long userId, long bookId, long oderDetailId);

    @Query(value = """
                SELECT
                    b.book_id AS bookId,
                    b.title AS title,
                    (
                        SELECT JSON_ARRAYAGG(
                            JSON_OBJECT('no', bi.book_image_no, 'path', bi.book_image_path, 'imageType', bi.image_type)
                        )
                        FROM book_images bi
                        WHERE bi.book_id = b.book_id
                    ) AS images,
                    r.order_detail_id AS orderDetailId,
                    r.review_id AS reviewId
                FROM books b
                LEFT JOIN reviews r ON b.book_id = r.book_id
                    AND r.user_created_id = :userId
                    AND r.order_detail_id IN (:orderDetailIds)
                WHERE b.book_id IN (:bookIds)
            """,
            nativeQuery = true)
    List<BookReviewProjection> getBookReviewProjectionList(@Param("userId") long userId, @Param("bookIds") List<Long> bookIds, @Param("orderDetailIds") List<Long> orderDetailIds);

    Long countAllByCreatedAtAfterOrModifiedAtAfter(ZonedDateTime createdAtAfter, ZonedDateTime modifiedAtAfter);
}
