package com.daisobook.shop.booksearch.BooksSearch.repository.policy;

import com.daisobook.shop.booksearch.BooksSearch.dto.projection.DiscountValueListProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.DiscountValueProjection;
import com.daisobook.shop.booksearch.BooksSearch.entity.policy.DiscountPolicy;
import com.daisobook.shop.booksearch.BooksSearch.entity.policy.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface DiscountPolicyRepository extends JpaRepository<DiscountPolicy, Long> {
    @Query(value = "SELECT p.policy_id id, p.policy_name name, p.discount_type discountType, p.discount_value 'value' FROM discount_policies p WHERE p.target_type = :targetType AND (p.target_id IS NULL OR p.target_id = :targetId) AND p.is_active = :isActive AND p.start_date <= now() AND (p.end_date IS NULL OR p.end_date >= now())", nativeQuery = true )
    List<DiscountValueProjection> findAllByTargetTypeAndTargetIdAndIsActive(@Param("targetType") TargetType targetType, @Param("targetId") Long targetId, @Param("isActive") boolean isActive);

    @Query(value = "SELECT p.policy_id id, p.policy_name name, p.discount_type discountType, p.discount_value 'value' FROM discount_policies p WHERE p.target_type = :targetType AND p.target_id IN (:targetIds) AND p.is_active = :isActive AND p.start_date <= now() AND (p.end_date IS NULL OR p.end_date >= now())", nativeQuery = true)
    List<DiscountValueProjection> findAllByTargetTypeAndTargetIdInAndIsActive(@Param("targetType") TargetType targetType, @Param("targetIds") Collection<Long> targetIds, @Param("isActive") boolean isActive);

    @Query(value = """
            SELECT
            b.book_id as bookId,
            (
                SELECT JSON_ARRAYAGG(
                     JSON_OBJECT('id', val.policy_id, 'name', val.policy_name, 'discountType', val.discount_type, 'value', val.discount_value)
                  )
                FROM(
                    -- 전체 할인
                    SELECT p.policy_id, p.policy_name, p.discount_type, p.discount_value
                    FROM discount_policies p
                    WHERE p.target_type = 'GLOBAL'
                        AND p.is_active = TRUE
                        AND p.start_date <= now()
                        AND (p.end_date IS NULL OR p.end_date >= now())
            
                    UNION ALL
                    -- 카테고리 할인
                    SELECT p.policy_id, p.policy_name, p.discount_type, p.discount_value
                    FROM discount_policies p
                    JOIN book_categories bc ON p.target_id = bc.category_id
                    WHERE p.target_type = 'CATEGORY'
                        AND p.is_active = TRUE
                        AND p.start_date <= now()
                        AND (p.end_date IS NULL OR p.end_date >= now())
                        AND bc.book_id = b.book_id
            
                    UNION ALL
                    -- 출판사 할인
                    SELECT p.policy_id, p.policy_name, p.discount_type, p.discount_value
                    FROM discount_policies p
                    WHERE p.target_type = 'PUBLISHER'
                        AND p.is_active = TRUE
                        AND p.start_date <= now()
                        AND (p.end_date IS NULL OR p.end_date >= now())
                        AND p.target_id = b.publisher_id
            
                    UNION ALL
                    -- 상품 개별 할인
                    SELECT p.policy_id, p.policy_name, p.discount_type, p.discount_value
                    FROM discount_policies p
                    WHERE p.target_type = 'PRODUCT'
                        AND p.is_active = TRUE
                        AND p.start_date <= now()
                        AND (p.end_date IS NULL OR p.end_date >= now())
                        AND p.target_id = b.book_id
                ) as val
            ) AS discountValueList
            FROM books b
            WHERE b.book_id IN (:bookIds)

            """,
            nativeQuery = true
    )
    List<DiscountValueListProjection> getDiscountValue(@Param("bookIds") List<Long> bookIds);
}
