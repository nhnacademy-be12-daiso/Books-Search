package com.daisobook.shop.booksearch.BooksSearch.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
        UserApiClient.class,
        // Feign에 필요한 최소한의 인프라 빈만 로드
        org.springframework.cloud.openfeign.FeignAutoConfiguration.class,
        org.springframework.cloud.loadbalancer.config.LoadBalancerAutoConfiguration.class,
        org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class
})
@EnableFeignClients(clients = UserApiClient.class)
@ActiveProfiles("test")
class UserApiClientTest {

    // 1. @AutoConfigureWireMock 대신 이 코드를 사용합니다.
    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort()) // 랜덤 포트 사용
            .build();

    @Autowired
    private UserApiClient userApiClient;

    // 2. 중요: WireMock이 잡은 랜덤 포트를 Feign Client URL에 꽂아줍니다.
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.openfeign.client.config.TEAM3-USER.url",
                () -> "http://localhost:" + wireMock.getPort());
    }

    @Test
    void earnPointByPolicy_Success_Test() {
        // 3. Stub 설정 (wireMock.stubFor 사용)
        wireMock.stubFor(post(urlPathEqualTo("/api/internal/points/policy"))
                .willReturn(aResponse()
                        .withStatus(200)));

        // 4. 실행
        ResponseEntity<Void> response = userApiClient.earnPointByPolicy(1L, "WELCOME");

        // 5. 검증
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }
}