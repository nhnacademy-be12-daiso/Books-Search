package com.daisobook.shop.booksearch.books_search.service.like;

import com.daisobook.shop.booksearch.books_search.dto.projection.LikeBookListProjection;
import com.daisobook.shop.booksearch.books_search.dto.response.LikeRespDTO;
import com.daisobook.shop.booksearch.books_search.entity.book.Book;
import com.daisobook.shop.booksearch.books_search.entity.like.Like;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface LikeService {
    void existLike(long bookId, long userId);
    void createLike(long userId, Book book);
    List<LikeRespDTO> getLikeList(long userId);
//    void updateLike(long likeId);// 필요한가?
    void deleteLike(long userId, Book book);
    //book 서비스에서 사용하는 메서드
    int likeCount(long bookId);
    boolean likeCheck(long bookId, Long userId);
    Set<Long> getLikeByUserIdAndBookIds(Long userId, List<Long> bookIds);
    List<Like> getBookIsLike(Long userId, List<Book> books);

//    //v2
    void saveLike(Like like);
    void deleteLike(long bookId, long userId);
    List<LikeBookListProjection> getMyLikeList(long userId, Pageable pageable);
}
