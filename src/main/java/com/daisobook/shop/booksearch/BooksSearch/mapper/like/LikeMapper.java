package com.daisobook.shop.booksearch.BooksSearch.mapper.like;

import com.daisobook.shop.booksearch.BooksSearch.dto.DiscountValueListData;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.LikeBookListProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.LikeListRespDTO;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
import java.util.Map;

public interface LikeMapper {
    List<LikeListRespDTO> toLikeListRespDTOList(List<LikeBookListProjection> listProjectionList, Map<Long, List<DiscountValueListData>> discountPolicyByDataMap) throws JsonProcessingException;
}
