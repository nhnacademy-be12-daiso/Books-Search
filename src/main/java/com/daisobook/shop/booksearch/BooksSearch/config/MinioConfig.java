package com.daisobook.shop.booksearch.BooksSearch.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class MinioConfig {

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.secretKey}")
    private String secretKey;
    
    // WebClient를 위한 Builder도 Bean으로 등록합니다.
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(accessKey, secretKey)
                // HTTPS를 사용하지 않는 경우 secure(false)를 설정합니다. 
                // 운영 환경에서는 반드시 secure(true)를 사용해야 합니다.
//                .secure(minioUrl.startsWith("https"))
                .build();
    }
}