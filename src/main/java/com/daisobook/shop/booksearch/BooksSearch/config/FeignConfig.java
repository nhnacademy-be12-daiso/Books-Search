package com.daisobook.shop.booksearch.BooksSearch.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.daisobook.shop.booksearch") // 패키지 경로에 맞게 조정하세요
public class FeignConfig {
    // 외부 API 호출 활성화
}