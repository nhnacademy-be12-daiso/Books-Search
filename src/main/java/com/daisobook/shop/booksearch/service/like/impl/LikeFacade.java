package com.daisobook.shop.booksearch.service.like.impl;

import com.daisobook.shop.booksearch.dto.DiscountDTO;
import com.daisobook.shop.booksearch.dto.projection.LikeBookListProjection;
import com.daisobook.shop.booksearch.dto.response.like.LikeListRespDTO;
import com.daisobook.shop.booksearch.dto.response.like.MyLikeList;
import com.daisobook.shop.booksearch.entity.book.Book;
import com.daisobook.shop.booksearch.entity.like.Like;
import com.daisobook.shop.booksearch.exception.custom.book.NotFoundBook;
import com.daisobook.shop.booksearch.exception.custom.mapper.FailObjectMapper;
import com.daisobook.shop.booksearch.mapper.like.LikeMapper;
import com.daisobook.shop.booksearch.service.book.impl.BookCoreService;
import com.daisobook.shop.booksearch.service.like.LikeService;
import com.daisobook.shop.booksearch.service.policy.DiscountPolicyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class LikeFacade {
    private final LikeService likeService;
    private final BookCoreService bookService;
    private final DiscountPolicyService discountPolicyService;
    private final LikeMapper likeMapper;

    @Transactional
    public void addLike(long bookId, long userId){
        likeService.existLike(bookId, userId);

        Book book = bookService.getBook_Id(bookId);

        if(book == null){
            log.error("[좋아요 생성] 존재하지 않은 도서 좋아요 시도 - 도서Id:{}", bookId);
            throw new NotFoundBook("[좋아요 생성] 존재하지 않는 도서의 좋아요 시도입니다.");
        }

        Like newLike = new Like(book, userId);

        likeService.saveLike(newLike);
    }

    @Transactional(readOnly = true)
    public MyLikeList getMyLikeList(long userId){
        Pageable pageable = PageRequest.of(0,10);
        List<LikeBookListProjection> myLikeList = likeService.getMyLikeList(userId, pageable);

        Map<Long, DiscountDTO.Request> discountDTOMap = likeMapper.toDiscountDTOMap(myLikeList);
        Map<Long, DiscountDTO.Response> discountPriceMap = null;
        try {
            discountPriceMap = discountPolicyService.getDiscountPriceMap(discountDTOMap);
        } catch (JsonProcessingException e) {
            log.error("[좋아요 리스트] 할인 정책 매핑을 실패했습니다");
            throw new FailObjectMapper(e.getMessage());
        }
        if(discountPriceMap == null){
            return null;
        }

        List<LikeListRespDTO> listRespDTOList = null;
        try {
            listRespDTOList = likeMapper.toLikeListRespDTOList(myLikeList, discountPriceMap);
        } catch (JsonProcessingException e) {
            throw new FailObjectMapper(e.getMessage());
        }
        return new MyLikeList(listRespDTOList);
    }

    @Transactional
    public void deleteLike(long bookId, long userId){
        likeService.deleteLike(bookId, userId);
    }
}
