package com.daisobook.shop.booksearch.BooksSearch.service.like;

import com.daisobook.shop.booksearch.BooksSearch.dto.response.LikeRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.like.Like;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface LikeService {
    void createLike(long userId, Book book);
    List<LikeRespDTO> getLikeList(long userId);
//    void updateLike(long likeId);// 필요한가?
    void deleteLike(long userId, Book book);
    //book 서비스에서 사용하는 메서드
    int likeCount(long bookId);
    boolean likeCheck(long bookId, Long userId);
    Set<Long> getLikeByUserIdAndBookIds(Long userId, List<Long> bookIds);
    List<Like> getBookIsLike(Long userId, List<Book> books);
}
