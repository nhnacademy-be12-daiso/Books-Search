package com.daisobook.shop.booksearch.BooksSearch.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.function.client.WebClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*; // WireMock의 post를 위해 반드시 필요
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
        AiClient.class,
        org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class,
        org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration.class
})
@Import(AiClientTest.WebClientTestConfig.class) // WebClient 빈 주입용
@ActiveProfiles("test")
class AiClientTest {

    // 테스트용 WebClient 빈 설정
    @TestConfiguration
    static class WebClientTestConfig {
        @Bean
        public WebClient webClient() {
            return WebClient.builder().build();
        }
    }

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @Autowired
    private AiClient aiClient;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("app.ai.gemini-url", () -> "http://localhost:" + wireMock.getPort() + "/v1/models/gemini-pro:generateContent");
        registry.add("app.ai.gemini-api-key", () -> "test-key");
    }

    @Test
    @DisplayName("Gemini API 호출 및 결과 파싱 테스트")
    void generateAnswer_Success_Test() {
        // 1. 가짜 Gemini 응답 JSON
        String mockResponse = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [{ "text": "{\\"title\\": \\"정제된 도서 제목\\"}" }]
                      }
                    }
                  ]
                }
                """;

        // 2. WireMock Stub 설정 (중요: static import 확인)
        wireMock.stubFor(post(urlPathMatching("/v1/models/gemini-pro:generateContent.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockResponse)));

        // 3. 실행
        String result = aiClient.generateAnswer("테스트 프롬프트");

        // 4. 검증
        assertThat(result).contains("정제된 도서 제목");
    }
}