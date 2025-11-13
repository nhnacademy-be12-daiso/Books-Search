package com.daisobook.shop.booksearch.BooksSearch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RerankResult {
    private int index; // 원본 candidates 리스트에서의 인덱스
    private double score; // 재순위화 점수
}