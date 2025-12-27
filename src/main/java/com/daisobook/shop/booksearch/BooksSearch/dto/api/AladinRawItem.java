package com.daisobook.shop.booksearch.BooksSearch.dto.api;

public record AladinRawItem(
        String title,           // 제목
        String author,          // 저자 (전체 문자열: "배근성, 영진연구소 지은이")
        String description,     // 요약 설명
        String isbn13,          // ISBN13
        String publisher,       // 출판사
        String pubDate,         // 출판일 (예: "2022-03-10")
        Long priceStandard,     // 정가
        String categoryName,    // 알라딘 카테고리 경로
        String cover,           // 표지 이미지 URL (가장 중요!)
        String link,            // 알라딘 상품 상세 페이지 URL (필요 시)
        String customerReviewRank // 별점/평점 (태그 생성 시 참고용)
) {}