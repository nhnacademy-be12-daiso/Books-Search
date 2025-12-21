package com.daisobook.shop.booksearch.BooksSearch.search.component.management;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookRedisPendingService {

    private final StringRedisTemplate redis;

    // 임베딩 재생성 대기 도서의 Redis Set 키
    private static final String KEY_EMBEDDING_PENDING = "booksearch:pending:embedding";

    // 임베딩 재생성 대기 도서에 ISBN 추가
    public void addEmbeddingPending(String isbn) {
        redis.opsForSet().add(KEY_EMBEDDING_PENDING, isbn);
    }
}
