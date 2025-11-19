package com.daisobook.shop.booksearch.BooksSearch.service.like;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.LikeReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.LikeRespDTO;

import java.util.List;

public interface LikeService {
    void exist(long likeId);
    void createLike(LikeReqDTO likeReqDTO);
    List<LikeRespDTO> getLikeList(long userId);
    int likeCount(long bookId);
    boolean likeCheck(long bookId, long userId);
    void updateLike(long likeId);
    void deleteLike(long likeId);
}
