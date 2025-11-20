package com.daisobook.shop.booksearch.BooksSearch.repository;

import com.daisobook.shop.booksearch.BooksSearch.dto.response.LikeRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LikeRepository extends JpaRepository<Like, Long> {
    boolean existsLikeByBookIdAndUserId(long bookId, long userId);

    boolean existsLikeById(long id);

    List<Like> findAllByUserId(long userId);

    int countAllByBook_Id(long bookId);

    boolean existsLikeByBook_IdAndUserId(long bookId, long userId);

    Like findLikeById(long id);

    void deleteLikeById(long id);
}
