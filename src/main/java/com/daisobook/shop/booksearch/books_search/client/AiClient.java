package com.daisobook.shop.booksearch.books_search.client;

import com.daisobook.shop.booksearch.books_search.dto.api.AladinRawItem;
import com.daisobook.shop.booksearch.books_search.dto.response.RoleNameListRespDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.category.CategoryList;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiClient {
    private final WebClient webClient;
    private final ObjectMapper objectMapper; // JSON 파싱용

    @Value("${app.ai.gemini-url}")
    private String geminiUrl;
    @Value("${app.ai.gemini-api-key}")
    private String geminiApiKey;

    // ... 기존 generateEmbedding, rerank 메서드들 ...

    public String refineBookData(AladinRawItem rawItem, CategoryList categoryList, RoleNameListRespDTO roleNameList) {
        // 1. 카테고리 경로 목록을 문자열로 변환 (AI 참고용)
        String categoryOptions = categoryList.categoryPathList().stream()
                .map(path -> String.format("ID: %d | 경로: %s", path.categoryId(), path.path()))
                .collect(Collectors.joining("\n"));

        String roleNameOptions = String.join(", ", roleNameList.roleNames());

        String prompt = String.format("""
    ### [역할]
    대한민국 도서 서지정보 전문가로서, 제공된 '알라딘 데이터'를 우리 시스템의 'JSON 규격'으로 정제하라.

    ### [필독: 저자 누락 주의보]
    현재 시스템에서 '영진정보연구소'와 같은 기관 저자가 출판사명(%s)과 유사하다는 이유로 자꾸 누락되고 있다. 
    **명령: 출판사와 이름이 비슷하더라도 저자 리스트에서 절대 제외하지 마라. 둘은 엄연히 별개의 데이터다.**

    ### [입력 데이터]
    - 제목: %s
    - 저자 원본: %s
    - 출판일: %s (YYYY-MM-DD 형식으로 변환)
    - 정가: %d
    - 출판사: %s
    - 이미지 URL: %s
    - 알라딘 분류: %s
    - 도서 설명: %s

    ### [필수 보강 및 정제 지침]
    1. **저자(authorList) 무조건 추출 (알고리즘 준수)**:
       - '저자 원본' 문자열("%s")을 오직 **쉼표(,)**를 기준으로 강제 분리하라.
       - 분리된 모든 항목(사람, 연구소, 편집부 등)은 각각 하나의 독립된 객체로 만든다.
       - 기관명(예: 영진정보연구소) 뒤에 역할이 없으면, 함께 나열된 다른 저자의 역할을 복사하거나 우리 시스템 역할 목록(%s) 중 '지은이' 또는 '지음'을 부여하라.
       - **최종 검증**: 입력 데이터에 "배근성"과 "영진정보연구소"가 있다면, JSON 결과의 `authorList`에도 반드시 두 개 모두 존재해야 한다.
    
    2. **발행일/가격**: 실제 날짜(%s)와 가격(%d)을 사용하라.
    3. **이미지 매핑**: '이미지 URL'을 'path'에 넣고, 'imageType'은 'COVER'로 고정하라. (허용: COVER, DETAIL, REVIEW)
    4. **카테고리 계층화**: 우리 시스템 목록에서 가장 적절한 하위 카테고리를 선택하되, 부모 계층까지 포함하라. (preCategoryId 필드명 준수)
    5. **태그 생성**: 'tagName' 객체 형태로 5개 이상 추출하라.

    [데이터 생성 미션: 목차(index)]
    - 원본 목차가 없다면 제목("%s")과 설명("%s")을 분석하여 5~8개 챕터로 **직접 생성(추론)**하라. 절대 비워두지 마라.

    [데이터 생성 미션: 설명(description)]
    - 설명이 짧으면 제목과 카테고리를 바탕으로 3문장 이상 풍부하게 작성하라.

    ### [참조 목록]
    - 카테고리: %s
    - 역할 목록: %s

    ### [출력 JSON 규격 (JSON만 응답)]
    {
      "isbn": "%s",
      "title": "...",
      "index": "...",
      "description": "...",
      "authorList": [
        { "authorName": "배근성", "roleName": "지은이" },
        { "authorName": "영진정보연구소", "roleName": "지은이" }
      ],
      "publisher": "...",
      "publicationDate": "YYYY-MM-DD",
      "price": 0,
      "volumeNo": 1,
      "imageList": [{ "no": 1, "path": "URL", "imageType": "COVER" }],
      "categories": [{ "categoryId": 0, "categoryName": "...", "deep": 0, "preCategoryId": null }],
      "tags": [{ "tagName": "..." }]
    }
    """,
                rawItem.publisher(), // 저자 누락 주의보용
                rawItem.title(), rawItem.author(), rawItem.pubDate(), rawItem.priceStandard(),
                rawItem.publisher(), rawItem.cover(), rawItem.categoryName(), rawItem.description(),
                rawItem.author(), roleNameOptions, // 저자 알고리즘용
                rawItem.pubDate(), rawItem.priceStandard(),
                rawItem.title(), rawItem.description(),
                categoryOptions, roleNameOptions,
                rawItem.isbn13());

        return generateAnswer(prompt);
    }

    public String generateAnswer(String prompt) {
        try {
            GeminiRequest request = new GeminiRequest(List.of(new Content(List.of(new Part(prompt)))));

            GeminiResponse response = webClient.post()
                    .uri(geminiUrl + "?key=" + geminiApiKey)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .block();

            if (response != null && !response.candidates().isEmpty()) {
                return response.candidates().get(0).content().parts().get(0).text();
            }
            return "{}";
        } catch (Exception e) {
            log.warn("[AiClient] Gemini 응답 지연 또는 오류. AI 추천 없이 진행.: {}", e.getMessage());
            return "{}";
        }
    }

    // DTO Records
    record GeminiRequest(List<Content> contents) {}
    record Content(List<Part> parts) {}
    record Part(String text) {}
    record GeminiResponse(List<Candidate> candidates) {}
    record Candidate(Content content) {}
}