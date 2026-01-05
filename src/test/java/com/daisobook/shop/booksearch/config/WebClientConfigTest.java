package com.daisobook.shop.booksearch.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {MinioConfig.class, WebClientConfig.class}) // Builder가 있는 MinioConfig도 포함
class WebClientConfigTest {

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("WebClient 빈이 정상적으로 생성되고 컨텍스트에 등록되어야 한다")
    void webClient_Bean_Registration_Test() {
        // when
        WebClient webClient = context.getBean(WebClient.class);

        // then
        assertThat(webClient).isNotNull();
    }

    @Test
    @DisplayName("WebClient 빈이 중복되지 않고 단일 객체(Singleton)로 존재해야 한다")
    void webClient_Singleton_Test() {
        // when
        WebClient webClient1 = context.getBean(WebClient.class);
        WebClient webClient2 = context.getBean(WebClient.class);

        // then
        assertThat(webClient1).isSameAs(webClient2);
    }
}