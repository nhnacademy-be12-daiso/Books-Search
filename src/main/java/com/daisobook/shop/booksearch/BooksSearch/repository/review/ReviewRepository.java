package com.daisobook.shop.booksearch.BooksSearch.repository.review;

import com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookReviewProjection;
import com.daisobook.shop.booksearch.BooksSearch.entity.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsReviewByBook_IdAndUserIdAndOderDetailId(long bookId, long userId, long oderDetailId);

    Review findReviewById(long id);

    List<Review> findAllByUserId(long userId);

    List<Review> findAllByBook_Id(long bookId);

    Review findReviewByUserIdAndBook_IdAndOderDetailId(long userId, long bookId, long oderDetailId);

    @Query(value = """
                SELECT
                    (
                        SELECT JSON_OBJECT(
                            'bookId', b.book_id, 'title', b.title,
                            'imageList',
                            (
                                SELECT JSON_ARRAYAGG(
                                JSON_OBJECT('no', bi.book_image_no, 'path', bi.book_image_path, 'imageType', bi.image_type)
                                )
                                FROM book_images bi
                                WHERE bi.book_id = b.book_id
                            )
                        )
                        FROM books b
                        WHERE r.book_id = b.book_id
                    ) as book,
                    r.order_detail_id as orderDetailId,
                    r.review_id as reviewId
                FROM reviews r
                WHERE r.user_created_id = :userId
                    AND r.book_id IN (:bookIds)
                    AND r.order_detail_id IN (:orderDetailIds)
            """,
            nativeQuery = true)
    List<BookReviewProjection> getBookReviewProjectionList(@Param("userId") long userId, @Param("bookIds") List<Long> bookIds, @Param("orderDetailIds") List<Long> orderDetailIds);
}
