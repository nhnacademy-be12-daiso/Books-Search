package com.daisobook.shop.booksearch.BooksSearch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // 생성일, 수정일 자동화를 위한 설정
}