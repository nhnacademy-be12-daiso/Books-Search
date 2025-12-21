package com.daisobook.shop.booksearch.BooksSearch.search.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;


@Service
@RequiredArgsConstructor
public class PendingWorkService {

    private final StringRedisTemplate redis;

    @Value("${booksearch.redis.embedding-set-key:booksearch:pending:embedding}")
    private String embeddingSetKey;

    @Value("${booksearch.redis.ai-cooldown-prefix:booksearch:cooldown:ai:test:}")
    private String aiCooldownPrefix;

    @Value("${booksearch.redis.ai-cooldown-ttl:PT6H}")
    private Duration aiCooldownTtl;

    /** 임베딩 재생성 후보 ISBN을 Set에 적재 */
    public void addEmbeddingCandidate(String isbn) {
        redis.opsForSet().add(embeddingSetKey, isbn);
    }

    /**
     * 동일 ISBN에 대해 AI 분석 MQ가 너무 자주 나가지 않도록 쿨다운.
     * @return true면 이번에 발행해도 됨
     */
    public boolean canPublishAi(String isbn) {
        String key = aiCooldownPrefix + isbn;
        Boolean ok = redis.opsForValue().setIfAbsent(key, "1", aiCooldownTtl);
        return Boolean.TRUE.equals(ok);
    }
}
