package com.daisobook.shop.booksearch.books_search.config;

import io.minio.MinioClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = MinioConfig.class)
@ActiveProfiles("test") // src/test/resources/application-test.yml 설정 사용
class MinioConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("MinioClient 빈이 정상적으로 생성되고 컨텍스트에 등록되어야 한다")
    void minioClient_Bean_Registration_Test() {
        // when
        MinioClient minioClient = applicationContext.getBean(MinioClient.class);

        // then
        assertThat(minioClient).isNotNull();
    }

    @Test
    @DisplayName("WebClient.Builder 빈이 정상적으로 생성되어야 한다")
    void webClientBuilder_Bean_Registration_Test() {
        // when
        WebClient.Builder builder = applicationContext.getBean(WebClient.Builder.class);

        // then
        assertThat(builder).isNotNull();
    }
}