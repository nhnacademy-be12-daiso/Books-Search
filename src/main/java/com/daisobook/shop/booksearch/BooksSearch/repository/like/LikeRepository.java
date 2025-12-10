package com.daisobook.shop.booksearch.BooksSearch.repository.like;

import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.like.Like;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
