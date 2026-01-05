package com.daisobook.shop.booksearch.books_search.mapper.like;

import com.daisobook.shop.booksearch.books_search.dto.DiscountDTO;
import com.daisobook.shop.booksearch.books_search.dto.projection.LikeBookListProjection;
import com.daisobook.shop.booksearch.books_search.dto.response.like.LikeListRespDTO;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
import java.util.Map;

public interface LikeMapper {
    List<LikeListRespDTO> toLikeListRespDTOList(List<LikeBookListProjection> listProjectionList, Map<Long, DiscountDTO.Response> discountResponseMap) throws JsonProcessingException;
    Map<Long, DiscountDTO.Request> toDiscountDTOMap(List<LikeBookListProjection> listProjectionList);;
}
