package com.daisobook.shop.booksearch.BooksSearch.repository.like;

import com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookIdProjection;
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

    @Query(value = "SELECT l.book_id as id FROM likes l WHERE l.user_id = :userId AND l.book_id IN (:bookIds)", nativeQuery = true)
    List<BookIdProjection> getLikeByUserIdAndBookIdIn(@Param("userId") long userId, @Param("bookIds") List<Long> bookIds);
}
