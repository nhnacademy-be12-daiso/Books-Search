package com.daisobook.shop.booksearch.BooksSearch.service.policy;

import com.daisobook.shop.booksearch.BooksSearch.dto.BookListData;
import com.daisobook.shop.booksearch.BooksSearch.dto.DiscountValueListData;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.DiscountValueProjection;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.policy.DiscountPolicy;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DiscountPolicyService {
    List<DiscountValueProjection> getDiscountValueByBookIdOrCategoryIdsOrPublisherId(List<Long> categoryIds, Long publisherId, Long bookId);
    Long getDiscountPrice(Book book);
    Map<Long, List<DiscountValueListData>> getDiscountPolicyByDataMap(List<Long> bookIds) throws JsonProcessingException;
    Map<Long, Long> getDiscountPriceMap(Map<Long, BookListData> bookListDataMap) throws JsonProcessingException;
}
