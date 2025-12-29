package com.daisobook.shop.booksearch.BooksSearch.search.discount;

import com.daisobook.shop.booksearch.BooksSearch.entity.policy.DiscountPolicy;
import com.daisobook.shop.booksearch.BooksSearch.entity.policy.TargetType;
import com.daisobook.shop.booksearch.BooksSearch.service.policy.DiscountPolicyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscountPolicyScheduling {

    private static final String KEY_PREFIX = "discount:policy:";
    private static final String ACTIVE_KEYS_INDEX = KEY_PREFIX + "active_keys";

    private static final String LOCK_KEY = "scheduler:discount-policy:lock";
    private static final String LOCK_VALUE = "LOCKED";

    private static final Duration LOCK_TTL = Duration.ofMinutes(5); // 락 유효시간

    private static final Duration POLICY_TTL = Duration.ofMinutes(15);  // 캐시 정책 유효시간

    private final DiscountPolicyService discountPolicyService;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(cron = "0 */5 * * * *", zone = "Asia/Seoul") // 5분마다
    public void refresh() {
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(LOCK_KEY, LOCK_VALUE, LOCK_TTL);

        if (Boolean.FALSE.equals(acquired)) {
            log.info("다른 서버에서 스케줄러가 이미 실행 중입니다. (Skip)");
            return;
        }

        try {
            doRefreshDiscountPolicyCache();
        } catch (Exception e) {
            log.error("Discount policy cache refresh failed", e);
        } finally {
            redisTemplate.delete(LOCK_KEY);
        }
    }

    private void doRefreshDiscountPolicyCache() {
        List<DiscountPolicy> active = discountPolicyService.getAllActiveDiscountPolicies();

        Set<String> desiredKeys = new HashSet<>(Math.max(active.size(), 16));

        Map<String, String> kv = new HashMap<>(Math.max(active.size(), 16));

        for (DiscountPolicy p : active) {
            String key = buildKey(p.getTargetType(), p.getTargetId());
            desiredKeys.add(key);

            DiscountPolicyCacheValue v = new DiscountPolicyCacheValue(
                    p.getTargetType().name(),
                    p.getTargetId(),           // GLOBAL이면 null일 수 있음(도메인에 맞게)
                    p.getDiscountType().name(),
                    p.getDiscountValue()
            );

            try {
                kv.put(key, objectMapper.writeValueAsString(v));
            } catch (Exception e) {
                log.error("Failed to serialize discount policy. key={}", key, e);
            }
        }

        // 기존 인덱스에 있던 키들 읽고, 없어져야 할 키 삭제
        Set<String> oldKeys = redisTemplate.opsForSet().members(ACTIVE_KEYS_INDEX);
        if (oldKeys == null) oldKeys = Collections.emptySet();

        List<String> toDelete = new ArrayList<>();
        for (String oldKey : oldKeys) {
            if (!desiredKeys.contains(oldKey)) {
                toDelete.add(oldKey);
            }
        }
        if (!toDelete.isEmpty()) {
            redisTemplate.delete(toDelete);
        }

        // 이번 라운드 값 세팅 + TTL
        for (Map.Entry<String, String> e : kv.entrySet()) {
            redisTemplate.opsForValue().set(e.getKey(), e.getValue(), POLICY_TTL);
        }

        // 인덱스 갱신 (완전 교체)
        redisTemplate.delete(ACTIVE_KEYS_INDEX);
        if (!desiredKeys.isEmpty()) {
            redisTemplate.opsForSet().add(ACTIVE_KEYS_INDEX, desiredKeys.toArray(String[]::new));
            redisTemplate.expire(ACTIVE_KEYS_INDEX, POLICY_TTL);
        }

        log.info("Discount policy cache refreshed. activePolicies={}, deletedKeys={}",
                desiredKeys.size(), toDelete.size());
    }

    private String buildKey(TargetType targetType, Long targetId) {
        return switch (targetType) {
            case GLOBAL -> KEY_PREFIX + "GLOBAL";
            case CATEGORY -> KEY_PREFIX + "CATEGORY:" + requireId(targetId, targetType);
            case PUBLISHER -> KEY_PREFIX + "PUBLISHER:" + requireId(targetId, targetType);
            case PRODUCT -> KEY_PREFIX + "PRODUCT:" + requireId(targetId, targetType);
        };
    }

    private Long requireId(Long targetId, TargetType t) {
        if (targetId == null) throw new IllegalArgumentException("targetId is null for " + t);
        return targetId;
    }

    public record DiscountPolicyCacheValue(
            String targetType,
            Long targetId,
            String discountType,
            Object discountValue
    ) {}
}
