package com.daisobook.shop.booksearch.BooksSearch.config;

import org.springframework.context.annotation.Configuration;
//import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ComponentScan.Filter;

@Configuration
// 1. JPA 설정: JPA 패키지만 보고, 혹시 섞여있을지 모르는 ES/Redis 타입은 무시
@EnableJpaRepositories(
        basePackages = "com.daisobook.shop.booksearch.BooksSearch.repository",
        includeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = org.springframework.data.jpa.repository.JpaRepository.class)
)
// 2. Elasticsearch 설정: ES 패키지만 정확히 타겟팅
//@EnableElasticsearchRepositories(
//        basePackages = "com.daisobook.shop.booksearch.BooksSearch.search.repository"
//)
public class DataConfig {
}