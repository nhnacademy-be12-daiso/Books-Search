package com.daisobook.shop.booksearch.BooksSearch.repository.policy;

import com.daisobook.shop.booksearch.BooksSearch.entity.policy.DiscountPolicy;
import com.daisobook.shop.booksearch.BooksSearch.entity.policy.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface DiscountPolicyRepository extends JpaRepository<DiscountPolicy, Long> {
    List<DiscountPolicy> findAllByTargetTypeAndTargetIdAndIsActive(TargetType targetType, Long targetId, boolean isActive);

    List<DiscountPolicy> findAllByTargetTypeAndTargetIdInAndIsActive(TargetType targetType, Collection<Long> targetIds, boolean isActive);
}
