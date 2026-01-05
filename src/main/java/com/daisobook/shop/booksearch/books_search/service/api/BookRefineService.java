package com.daisobook.shop.booksearch.books_search.service.api;

import com.daisobook.shop.booksearch.books_search.client.AiClient;
import com.daisobook.shop.booksearch.books_search.dto.api.AladinResponseWrapper;
import com.daisobook.shop.booksearch.books_search.dto.api.BookInfoDataView;
import com.daisobook.shop.booksearch.books_search.dto.response.RoleNameListRespDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.category.CategoryList;
import com.daisobook.shop.booksearch.books_search.exception.custom.ai.BookNotFoundException;
import com.daisobook.shop.booksearch.books_search.exception.custom.ai.LlmAnalysisException;
import com.daisobook.shop.booksearch.books_search.service.author.AuthorV2Service;
import com.daisobook.shop.booksearch.books_search.service.category.CategoryV2Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookRefineService {

    private final AiClient aiClient;
    private final CategoryV2Service categoryService; // 기존 카테고리 서비스
    private final AuthorV2Service authorService;
    private final ObjectMapper objectMapper;
    private final WebClient webClient = WebClient.create();

    @Value("${aladin.ttb-key}")
    private String ttbKey;

    public BookInfoDataView getRefinedBook(String isbn) {
        // 1. 알라딘 호출 (String으로 먼저 받아 로그를 찍고 직접 변환)
        String rawAladinJson = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http").host("www.aladin.co.kr").path("/ttb/api/ItemLookUp.aspx")
                        .queryParam("ttbkey", ttbKey)
                        .queryParam("itemIdType", "ISBN13")
                        .queryParam("ItemId", isbn)
                        .queryParam("output", "js").queryParam("Version", "20131101")
                        .build())
                .retrieve()
                .bodyToMono(String.class) // 일단 문자열로 받음
                .block();

        log.debug("알라딘 원본 응답 JSON: {}", rawAladinJson); // 여기서 알라딘이 주는 진짜 데이터를 확인!
        if (rawAladinJson == null || rawAladinJson.contains("ErrorCode")) {
            log.warn("알라딘 API 조회 실패 - ISBN: {}", isbn);
            throw new BookNotFoundException(isbn);
        }

        // 2. 문자열을 객체로 변환
        AladinResponseWrapper aladinRes;
        try {
            aladinRes = objectMapper.readValue(rawAladinJson, AladinResponseWrapper.class);
        } catch (Exception e) {
            log.error("알라딘 데이터 파싱 실패: {}", e.getMessage());
            return null;
        }

        if (aladinRes.item() == null || aladinRes.item().isEmpty()) {
            throw new BookNotFoundException(isbn);
        }

        // 3. 우리 시스템의 카테고리 경로 목록 조회
        CategoryList categoryList = categoryService.getCategoryList();
        RoleNameListRespDTO roleNameList = authorService.getRoleNameList();

        // 4. AI 가공 요청
        String rawAiResponse = null;

        // 5. JSON 정제 및 DTO 변환
        try {
            rawAiResponse = aiClient.refineBookData(aladinRes.item().getFirst(), categoryList, roleNameList);

            // 1. 마크다운 태그 제거
            String jsonResponse = rawAiResponse
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            if (jsonResponse.contains("\"publicationDate\": \"YYYY-MM-DD\"")) {
                // 알라딘에서 가져온 원본 날짜가 있다면 그것을 사용하고, 없으면 오늘 날짜 사용
                String fallbackDate = (aladinRes.item().getFirst().pubDate() != null) ? aladinRes.item().getFirst().pubDate() : "2025-01-01";
                jsonResponse = jsonResponse.replace("\"YYYY-MM-DD\"", "\"" + fallbackDate + "\"");
            }

            // 2. 추가 안전장치: JSON 시작점 '{' 이전의 모든 문자 제거
            if (jsonResponse.contains("{")) {
                jsonResponse = jsonResponse.substring(jsonResponse.indexOf("{"));
            }

            // 3. JSON 끝점 '}' 이후의 모든 문자 제거
            if (jsonResponse.lastIndexOf("}") != -1) {
                jsonResponse = jsonResponse.substring(0, jsonResponse.lastIndexOf("}") + 1);
            }

            log.info("정제된 JSON 결과: {}", jsonResponse); // 로그로 확인해보세요

            return objectMapper.readValue(jsonResponse, BookInfoDataView.class);
        } catch (Exception e) {
            log.error("AI 응답 원본: {}", rawAiResponse); // 에러 발생 시 AI가 뭐라고 했는지 로그 출력
            throw new LlmAnalysisException("도서 정보 가공 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}