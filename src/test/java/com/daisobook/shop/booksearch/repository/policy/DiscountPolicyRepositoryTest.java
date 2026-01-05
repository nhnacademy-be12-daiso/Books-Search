package com.daisobook.shop.booksearch.repository.policy;

import com.daisobook.shop.booksearch.entity.policy.DiscountPolicy;
import com.daisobook.shop.booksearch.entity.policy.DiscountType;
import com.daisobook.shop.booksearch.entity.policy.TargetType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DiscountPolicyRepositoryTest {

    @Autowired
    private DiscountPolicyRepository discountPolicyRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        // 1. 활성화된 전역(GLOBAL) 정책
        DiscountPolicy globalPolicy = new DiscountPolicy(
                "전체 10% 할인",
                DiscountType.PERCENTAGE, // 엔티티의 타입 확인 필요 (FIXED, PERCENTAGE 등)
                10L,
                TargetType.GLOBAL,
                null,
                ZonedDateTime.now().minusDays(1), // 이미 시작됨
                ZonedDateTime.now().plusDays(7),  // 아직 종료 안 됨
                true
        );
        entityManager.persist(globalPolicy);

        // 2. 특정 상품(PRODUCT) 정책
        DiscountPolicy productPolicy = new DiscountPolicy(
                "특정 도서 5000원 할인",
                DiscountType.FIXED_AMOUNT,
                5000L,
                TargetType.PRODUCT,
                1L, // targetId
                ZonedDateTime.now().minusDays(1),
                null, // 기간 제한 없음
                true
        );
        entityManager.persist(productPolicy);

        // 3. 비활성화된 정책 (조회되지 않아야 함)
        DiscountPolicy inactivePolicy = new DiscountPolicy(
                "지난 이벤트",
                DiscountType.FIXED_AMOUNT,
                1000L,
                TargetType.GLOBAL,
                null,
                ZonedDateTime.now().minusDays(10),
                ZonedDateTime.now().minusDays(1), // 이미 종료됨
                false
        );
        entityManager.persist(inactivePolicy);

        entityManager.flush();
        entityManager.clear();
    }

    //value가 h2에서 예약어인데 mysql 처럼 유연하게 대처를 못함
//    @Test
//    @DisplayName("타겟 타입과 ID로 현재 활성화된 할인 정책을 조회한다")
//    void findAllByTargetTypeAndTargetIdAndIsActiveTest() {
//        // When: 활성화된 PRODUCT 정책 조회
//        List<DiscountValueProjection> results = discountPolicyRepository
//                .findAllByTargetTypeAndTargetIdAndIsActive(TargetType.PRODUCT, 1L, true);
//
//        // Then
//        assertThat(results).hasSize(1);
//        assertThat(results.getFirst().getName()).isEqualTo("특정 도서 5000원 할인");
//    }

    @Test
    @DisplayName("전체 활성 정책 목록을 조회한다 (날짜 및 활성여부 필터링)")
    void findAllActivePoliciesTest() {
        // When
        List<DiscountPolicy> activePolicies = discountPolicyRepository.findAllActivePolicies();

        // Then: setUp에서 넣은 3개 중 기간이 맞고 isActive가 true인 것은 2개
        assertThat(activePolicies).hasSize(2);
        assertThat(activePolicies).extracting("policyName")
                .containsExactlyInAnyOrder("전체 10% 할인", "특정 도서 5000원 할인");
    }

//    @Test
//    @DisplayName("여러 타겟 ID 목록에 해당하는 정책을 조회한다")
//    void findAllByTargetTypeAndTargetIdInAndIsActiveTest() {
//        // When
//        List<DiscountValueProjection> results = discountPolicyRepository
//                .findAllByTargetTypeAndTargetIdInAndIsActive(TargetType.PRODUCT, List.of(1L, 2L, 3L), true);
//
//        // Then
//        assertThat(results).hasSize(1);
//        assertThat(results.getFirst().getValue()).isEqualTo(5000L);
//    }
}