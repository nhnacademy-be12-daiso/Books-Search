package com.daisobook.shop.booksearch.BooksSearch.service.like;

import com.daisobook.shop.booksearch.BooksSearch.dto.response.LikeRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;

import java.util.List;

public interface LikeService {
    void createLike(long userId, Book book);
    List<LikeRespDTO> getLikeList(long userId);
//    void updateLike(long likeId);// 필요한가?
    void deleteLike(long userId, Book book);
    //book 서비스에서 사용하는 메서드
    int likeCount(long bookId);
    boolean likeCheck(long bookId, Long userId);
}
