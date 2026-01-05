package com.daisobook.shop.booksearch.search.discount;

import com.daisobook.shop.booksearch.entity.policy.DiscountPolicy;
import com.daisobook.shop.booksearch.entity.policy.DiscountType;
import com.daisobook.shop.booksearch.entity.policy.TargetType;
import com.daisobook.shop.booksearch.service.policy.DiscountPolicyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscountPolicySchedulingTest {

    @Mock
    RedisTemplate<String, String> redisTemplate;

    @Mock
    ValueOperations<String, String> valueOps;

    @Mock
    SetOperations<String, String> setOps;

    @Mock
    DiscountPolicyService discountPolicyService;

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    DiscountPolicyScheduling scheduling;

    @BeforeEach
    void setup() {
        // 불필요한 스텁 경고를 피하기 위해 lenient() 사용.
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOps);
    }

    @Test
    void refresh_skips_when_lock_not_acquired() {
        when(valueOps.setIfAbsent(eq("scheduler:discount-policy:lock"), eq("LOCKED"), any(Duration.class)))
                .thenReturn(Boolean.FALSE);

        scheduling.refresh();

        verify(valueOps).setIfAbsent(eq("scheduler:discount-policy:lock"), eq("LOCKED"), any(Duration.class));
        verifyNoInteractions(discountPolicyService);
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void refresh_acquires_lock_and_refreshes_cache_and_deletes_old_keys_and_updates_index() throws Exception {
        when(valueOps.setIfAbsent(eq("scheduler:discount-policy:lock"), eq("LOCKED"), any(Duration.class)))
                .thenReturn(Boolean.TRUE);

        // 하나의 active 정책을 반환하는 경우
        DiscountPolicy p = mock(DiscountPolicy.class);
        when(p.getTargetType()).thenReturn(TargetType.GLOBAL);
        when(p.getTargetId()).thenReturn(null);
        // null로 두면 production 코드의 p.getDiscountType().name()에서 NPE 발생하므로 non-null enum 상수 사용
        when(p.getDiscountType()).thenReturn(DiscountType.class.getEnumConstants()[0]);
        when(p.getDiscountValue()).thenReturn(10.0);

        when(discountPolicyService.getAllActiveDiscountPolicies()).thenReturn(List.of(p));

        // 기존 인덱스에 있던 오래된 키 (삭제 대상)
        when(setOps.members("discount:policy:active_keys")).thenReturn(new HashSet<>(List.of("discount:policy:OLD")));

        // 직렬화 성공
        when(objectMapper.writeValueAsString(any())).thenReturn("{ }");

        // 실행
        scheduling.refresh();

        // lock 획득 호출
        verify(valueOps).setIfAbsent(eq("scheduler:discount-policy:lock"), eq("LOCKED"), any(Duration.class));
        // 새로운 키 세팅 확인 (GLOBAL 키)
        verify(valueOps).set(eq("discount:policy:GLOBAL"), eq("{ }"), any(Duration.class));
        // 오래된 키 삭제
        verify(redisTemplate).delete(List.of("discount:policy:OLD"));
        // 인덱스 완전 교체: 기존 인덱스 삭제, add, expire
        verify(redisTemplate).delete("discount:policy:active_keys");
        verify(setOps).add(eq("discount:policy:active_keys"), any(String[].class));
        verify(redisTemplate).expire(eq("discount:policy:active_keys"), any(Duration.class));
        // finally에서 lock 삭제
        verify(redisTemplate).delete("scheduler:discount-policy:lock");
    }

    @Test
    void refresh_deletes_lock_when_doRefresh_throws() {
        when(valueOps.setIfAbsent(eq("scheduler:discount-policy:lock"), eq("LOCKED"), any(Duration.class)))
                .thenReturn(Boolean.TRUE);

        when(discountPolicyService.getAllActiveDiscountPolicies()).thenThrow(new RuntimeException("db error"));

        scheduling.refresh();

        // 예외가 발생해도 finally 블록에서 lock 삭제됨
        verify(redisTemplate).delete("scheduler:discount-policy:lock");
    }

    @Test
    void serialization_failure_does_not_store_that_policy_but_others_continue() throws Exception {
        when(valueOps.setIfAbsent(eq("scheduler:discount-policy:lock"), eq("LOCKED"), any(Duration.class)))
                .thenReturn(Boolean.TRUE);

        DiscountPolicy good = mock(DiscountPolicy.class);
        when(good.getTargetType()).thenReturn(TargetType.PRODUCT);
        when(good.getTargetId()).thenReturn(123L);
        // non-null discount type
        when(good.getDiscountType()).thenReturn(DiscountType.class.getEnumConstants()[0]);
        when(good.getDiscountValue()).thenReturn(5.0);

        DiscountPolicy bad = mock(DiscountPolicy.class);
        when(bad.getTargetType()).thenReturn(TargetType.CATEGORY);
        when(bad.getTargetId()).thenReturn(99L);
        // non-null discount type
        when(bad.getDiscountType()).thenReturn(DiscountType.class.getEnumConstants()[0]);
        when(bad.getDiscountValue()).thenReturn(7.0);

        when(discountPolicyService.getAllActiveDiscountPolicies()).thenReturn(List.of(good, bad));

        // 주의: 일반 스텁을 먼저 등록하고, 그 다음에 category 전용 스텁을 등록해야 특정 케이스가 우선 적용됨
        when(objectMapper.writeValueAsString(any())).thenReturn("{good}");

        when(objectMapper.writeValueAsString(argThat(o -> {
            if (o == null) return false;
            return o.toString().contains("CATEGORY");
        }))).thenThrow(new RuntimeException("serialize fail"));

        when(setOps.members("discount:policy:active_keys")).thenReturn(Collections.emptySet());

        scheduling.refresh();

        // good 키는 세팅되어야 함
        verify(valueOps).set(eq("discount:policy:PRODUCT:123"), eq("{good}"), any(Duration.class));
        // bad 키는 직렬화 실패로 세팅 호출이 없어야 함
        verify(valueOps, never()).set(eq("discount:policy:CATEGORY:99"), anyString(), any(Duration.class));
    }

    @Test
    void private_buildKey_and_requireId_behavior() throws Exception {
        // Reflection으로 private buildKey 호출
        Method buildKey = DiscountPolicyScheduling.class.getDeclaredMethod("buildKey", TargetType.class, Long.class);
        buildKey.setAccessible(true);

        Object globalKey = buildKey.invoke(scheduling, TargetType.GLOBAL, null);
        assertEquals("discount:policy:GLOBAL", globalKey);

        Object prodKey = buildKey.invoke(scheduling, TargetType.PRODUCT, 42L);
        assertEquals("discount:policy:PRODUCT:42", prodKey);

        // CATEGORY with null id -> requireId should throw IllegalArgumentException (wrapped by reflection)
        Method requireId = DiscountPolicyScheduling.class.getDeclaredMethod("requireId", Long.class, TargetType.class);
        requireId.setAccessible(true);

        java.lang.reflect.InvocationTargetException ite = assertThrows(java.lang.reflect.InvocationTargetException.class,
                () -> requireId.invoke(scheduling, (Long) null, TargetType.CATEGORY));
        assertTrue(ite.getCause() instanceof IllegalArgumentException);

    }
}
