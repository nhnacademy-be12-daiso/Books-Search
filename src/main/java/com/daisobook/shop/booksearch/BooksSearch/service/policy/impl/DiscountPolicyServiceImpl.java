package com.daisobook.shop.booksearch.BooksSearch.service.policy.impl;

import com.daisobook.shop.booksearch.BooksSearch.entity.policy.DiscountPolicy;
import com.daisobook.shop.booksearch.BooksSearch.entity.policy.TargetType;
import com.daisobook.shop.booksearch.BooksSearch.repository.policy.DiscountPolicyRepository;
import com.daisobook.shop.booksearch.BooksSearch.service.policy.DiscountPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class DiscountPolicyServiceImpl implements DiscountPolicyService {
    private final DiscountPolicyRepository discountPolicyRepository;

    @Transactional
    @Override
    public List<DiscountPolicy> getDiscountPolicyByBookIdOrCategoryIdsOrPublisherId(List<Long> categoryIds, Long publisherId, Long bookId){
        List<DiscountPolicy> discountPolicies = new ArrayList<>(discountPolicyRepository.findAllByTargetTypeAndTargetIdAndIsActive(TargetType.GLOBAL, null, true));

        if(categoryIds != null && !categoryIds.isEmpty()){
            discountPolicies.addAll(discountPolicyRepository.findAllByTargetTypeAndTargetIdInAndIsActive(TargetType.CATEGORY, categoryIds, true));
        }
        if(publisherId != null) {
            discountPolicies.addAll(discountPolicyRepository.findAllByTargetTypeAndTargetIdAndIsActive(TargetType.PUBLISHER, publisherId, true));
        }
        if(bookId != null) {
            discountPolicies.addAll(discountPolicyRepository.findAllByTargetTypeAndTargetIdAndIsActive(TargetType.PRODUCT, bookId, true));
        }

        return discountPolicies;
    }
}
