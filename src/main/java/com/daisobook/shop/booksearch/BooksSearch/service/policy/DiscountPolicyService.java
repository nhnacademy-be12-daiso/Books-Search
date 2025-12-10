package com.daisobook.shop.booksearch.BooksSearch.service.policy;

import com.daisobook.shop.booksearch.BooksSearch.entity.policy.DiscountPolicy;

import java.util.List;

public interface DiscountPolicyService {
    List<DiscountPolicy> getDiscountPolicyByBookIdOrCategoryIdsOrPublisherId(List<Long> categoryIds, Long publisherId, Long bookId);
}
