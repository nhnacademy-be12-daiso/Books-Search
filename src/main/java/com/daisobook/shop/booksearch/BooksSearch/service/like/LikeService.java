package com.daisobook.shop.booksearch.BooksSearch.service.like;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.LikeReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.LikeRespDTO;

import java.util.List;

public interface LikeService {
    void exist(long likeId);
    void createLike(LikeReqDTO likeReqDTO);
    List<LikeRespDTO> getLikeList(long userId);
//    void updateLike(long likeId);// 필요한가?
    void deleteLike(long likeId);
    //book 서비스에서 사용하는 메서드
    int likeCount(long bookId);
    boolean likeCheck(long bookId, long userId);
}
