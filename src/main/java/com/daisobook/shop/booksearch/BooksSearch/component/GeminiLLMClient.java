package com.daisobook.shop.booksearch.BooksSearch.component;

import org.springframework.stereotype.Component;

@Component
public class GeminiLLMClient {
    // 실제 Google Generative AI SDK 사용 또는 REST API 호출
    
    // 컨텍스트를 받아 최종 응답을 생성
    public String generateResponse(String context) {
        // ... (Gemini API 호출 로직: context를 프롬프트로 전달)
        System.out.println("GeminiLLM: 최종 답변 생성 요청");

        return "Gemini가 재순위화된 도서 정보를 기반으로 생성한 답변: \n" + 
               "고객님께서는 자바 스프링 백엔드 도서를 추천해달라고 하셨습니다. 검색된 정보에 따르면...\n" + 
               "**'스프링 시큐리티로 JWT 구현'** 도서가 가장 적합합니다.";
    }
}