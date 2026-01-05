package com.daisobook.shop.booksearch.BooksSearch.service.policy;

import com.daisobook.shop.booksearch.BooksSearch.dto.DiscountDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.DiscountValueListData;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.DiscountValueProjection;
import com.daisobook.shop.booksearch.BooksSearch.entity.policy.DiscountPolicy;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
import java.util.Map;

public interface DiscountPolicyService {
    List<DiscountValueProjection> getDiscountValueByBookIdOrCategoryIdsOrPublisherId(List<Long> categoryIds, Long publisherId, Long bookId);
    List<DiscountValueListData> getDiscountPolicyByData(Long bookId) throws JsonProcessingException;
    Long getDiscountPrice(long bookId, Long price) throws JsonProcessingException;
    Map<Long, List<DiscountValueListData>> getDiscountPolicyByDataMap(List<Long> bookIds) throws JsonProcessingException;
    Map<Long, DiscountDTO.Response> getDiscountPriceMap(Map<Long, DiscountDTO.Request> discountDTORequestMap) throws JsonProcessingException;
    List<DiscountPolicy> getAllActiveDiscountPolicies();
}
