package com.daisobook.shop.booksearch.service.point.impl;

import com.daisobook.shop.booksearch.client.UserApiClient;
import com.daisobook.shop.booksearch.dto.point.PointPolicyType;
import com.daisobook.shop.booksearch.service.point.PointService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class PointServiceImpl implements PointService {

    private final UserApiClient userApiClient;

    @Override
    @Retryable(
            value = { FeignException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 3000, multiplier = 2) // 3초 간격으로 최대 3번 재시도
    )
    @Async("CustomTaskExecutor")
    public void requestReviewPoint(long userId, PointPolicyType type) {
        log.info("[Point Request] 유저 {} 포인트 적립 시도 (정책: {})", userId, type);

        // try-catch를 제거해야 @Retryable이 예외를 감지하고 재시도
        userApiClient.earnPointByPolicy(userId, type.name());

        log.info("[Point Success] 유저 {} 포인트 적립 완료", userId);
    }

    @Recover
    public void recover(FeignException e, long userId, PointPolicyType type) {
        log.error("[Final Point Failure] 재시도 결과 최종 실패. 유저: {}", userId);
        // TODO: 실패 전용 테이블에 저장하거나 관리자 알림
    }
}
