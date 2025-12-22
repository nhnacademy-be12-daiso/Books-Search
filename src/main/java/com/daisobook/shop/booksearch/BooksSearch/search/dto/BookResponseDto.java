package com.daisobook.shop.booksearch.BooksSearch.search.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

// 도서 응답 DTO
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookResponseDto {
    private String id;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private int price;
    private String description;

    @Builder.Default
    private List<String> categories = new ArrayList<>();

    private String imageUrl;

    /**
     * (기존 필드 유지) 매칭 점수(리랭킹 제거 이후엔 기본값/또는 ES score 기반으로 사용 가능)
     */
    @Setter
    private Integer matchRate;

    /**
     *  ES에 저장된 AI 분석 결과
     */
    @Setter
    private AiAnalysisDto aiResult;
}
