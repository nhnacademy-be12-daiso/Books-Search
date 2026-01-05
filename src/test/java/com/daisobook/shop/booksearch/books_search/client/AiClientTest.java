package com.daisobook.shop.booksearch.books_search.client;

import com.daisobook.shop.booksearch.books_search.dto.api.AladinRawItem;
import com.daisobook.shop.booksearch.books_search.dto.response.RoleNameListRespDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.category.CategoryList;
import com.daisobook.shop.booksearch.books_search.dto.response.category.CategoryPath;
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

import java.util.List;

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

    @Test
    @DisplayName("refineBookData: 모든 입력 데이터를 조합하여 정상적으로 프롬프트를 생성하고 응답을 받는지 테스트")
    void refineBookData_Success_Test() {
        // 1. Given: AladinRawItem 데이터 생성
        AladinRawItem rawItem = new AladinRawItem(
                "자바 마스터",
                "배근성, 영진정보연구소 지은이",
                "자바 프로그래밍의 기초부터 실무까지",
                "9788912345678",
                "영진출판사",
                "2024-01-05",
                25000L,
                "국내도서>컴퓨터/모바일>프로그래밍",
                "http://image.aladin.co.kr/cover/123.jpg",
                "http://www.aladin.co.kr/shop/wproduct.aspx?ItemId=1",
                "9"
        );

        // 2. Given: CategoryList 및 CategoryPath 생성
        CategoryPath path1 = new CategoryPath(101L, "프로그래밍", "국내도서 > 컴퓨터 > 프로그래밍");
        CategoryPath path2 = new CategoryPath(102L, "자바", "국내도서 > 컴퓨터 > 프로그래밍 > 자바");
        CategoryList categoryList = new CategoryList(List.of(path1, path2));

        // 3. Given: RoleNameListRespDTO 생성
        RoleNameListRespDTO roleNames = new RoleNameListRespDTO(List.of("지은이", "지음", "옮긴이", "감수"));

        // 4. Mock Gemini 응답 설정
        String expectedAiJson = "{\"isbn\": \"9788912345678\", \"title\": \"자바 마스터\"}";
        String mockResponse = String.format("""
        {
          "candidates": [{
            "content": { "parts": [{ "text": "%s" }] }
          }]
        }
        """, expectedAiJson.replace("\"", "\\\""));

        wireMock.stubFor(post(urlPathMatching("/v1/models/gemini-pro:generateContent.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockResponse)));

        // 5. When: 실행
        String result = aiClient.refineBookData(rawItem, categoryList, roleNames);

        // 6. Then: 검증
        assertThat(result).isEqualTo(expectedAiJson);

        // 프롬프트 내부의 핵심 데이터 전달 확인 (verify)
        wireMock.verify(postRequestedFor(urlPathMatching("/v1/models/gemini-pro:generateContent.*"))
                .withRequestBody(containing("9788912345678")) // ISBN 포함 확인
                .withRequestBody(containing("영진정보연구소")) // 저자 주의사항 키워드 확인
                .withRequestBody(containing("ID: 101 | 경로: 국내도서 > 컴퓨터 > 프로그래밍"))); // 카테고리 스트림 변환 확인
    }

    @Test
    @DisplayName("generateAnswer: API 서버가 500 에러를 반환할 때 catch 블록(빈 JSON) 작동 확인")
    void generateAnswer_ServerError_ReturnsEmptyJson() {
        // 1. Given: 500 Internal Server Error 스텁
        wireMock.stubFor(post(anyUrl())
                .willReturn(aResponse().withStatus(500)));

        // 2. When
        String result = aiClient.generateAnswer("에러 발생 프롬프트");

        // 3. Then
        assertThat(result).isEqualTo("{}");
    }

    @Test
    @DisplayName("generateAnswer: 응답은 성공했으나 후보군(candidates)이 비어있는 경우 처리")
    void generateAnswer_EmptyCandidates_ReturnsEmptyJson() {
        // 1. Given: candidates가 빈 리스트인 응답
        String emptyResponse = "{\"candidates\": []}";
        wireMock.stubFor(post(anyUrl())
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(emptyResponse)));

        // 2. When
        String result = aiClient.generateAnswer("빈 응답 테스트");

        // 3. Then
        assertThat(result).isEqualTo("{}");
    }

    @Test
    @DisplayName("generateAnswer: 네트워크 타임아웃 또는 연결 오류 시 예외 처리")
    void generateAnswer_NetworkError_ReturnsEmptyJson() {
        // 1. Given: 연결을 강제로 끊거나 잘못된 응답 전송
        wireMock.stubFor(post(anyUrl())
                .willReturn(aResponse().withFault(com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER)));

        // 2. When
        String result = aiClient.generateAnswer("네트워크 장애 테스트");

        // 3. Then
        assertThat(result).isEqualTo("{}");
    }
}