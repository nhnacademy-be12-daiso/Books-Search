package com.daisobook.shop.booksearch.BooksSearch.entity.policy;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "discount_policies")
public class DiscountPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id")
    private long id;

    @Setter
    @Column(name = "policy_name")
    private String policyName;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type")
    private DiscountType discountType;

    @Setter
    @Column(name = "discount_value")
    private double discountValue;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type")
    private TargetType targetType;

    @Setter
    @Column(name = "target_id")
    private Long targetId; //도서 ID, 카테고리 ID, 출판사 ID, 글로벌일때 null

    @Setter
    @Column(name = "start_date")
    private ZonedDateTime startDate;

    @Setter
    @Column(name = "end_date")
    private ZonedDateTime endDate;

    @Setter
    @Column(name = "is_active")
    private boolean isActive;
}
