package com.daisobook.shop.booksearch.BooksSearch.repository.like;

import com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookIdProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.LikeBookListProjection;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.like.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface LikeRepository extends JpaRepository<Like, Long> {
    boolean existsLikeByBookIdAndUserId(long bookId, long userId);

    boolean existsLikeById(long id);

    List<Like> findAllByUserId(long userId);

    int countAllByBook_Id(long bookId);

    boolean existsLikeByBook_IdAndUserId(long bookId, long userId);

    void deleteLikeById(long id);

    void deleteLikeByBookAndUserId(Book book, long userId);

    List<Like> findAllByUserIdAndBookIn(long userId, Collection<Book> books);

    @Query(value = "SELECT l.book_id as id FROM likes l WHERE l.user_created_id = :userId AND l.book_id IN (:bookIds)", nativeQuery = true)
    List<BookIdProjection> getLikeByUserIdAndBookIdIn(@Param("userId") long userId, @Param("bookIds") List<Long> bookIds);

    Like findLikeByBook_IdAndIdAndUserId(long bookId, long id, long userId);

    @Query(value = """
            SELECT
                l.like_id as likeId,
                b.book_id as bookId,
                b.isbn,
                b.title,
                (
                    SELECT JSON_ARRAYAGG(
                        JSON_OBJECT('authorId', a.author_id, 'authorName', a.author_name, 'roleId', r.role_id, 'roleName', r.role_name)
                        )
                    FROM book_authors ba
                    LEFT JOIN authors a ON ba.author_id = a.author_id
                    LEFT JOIN roles r ON ba.role_id = r.role_id
                    WHERE ba.book_id = b.book_id
                ) AS authors,
                b.price,
                b.status,
                (
                    SELECT
                        bi.book_image_path as image
                    FROM book_images bi
                    WHERE bi.book_id = b.book_id AND bi.book_image_no = 1
                ) AS image,
                b.volume_no as volumeNo,
                b.is_packaging as packaging,
                DATE_FORMAT(l.created_at, '%Y-%m-%dT%H:%i:%s.%f+09:00') as createdAt
            FROM likes l
            JOIN books b ON b.book_id = l.book_id
            WHERE l.user_created_id = :userId
            ORDER BY l.created_at DESC
            """,
            nativeQuery = true)
    List<LikeBookListProjection> getAllByUserId(@Param("userId") long userId);

    Like findLikeByBook_IdAndUserId(long bookId, long userId);
}
