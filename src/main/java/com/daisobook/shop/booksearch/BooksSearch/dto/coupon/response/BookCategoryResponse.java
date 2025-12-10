package com.daisobook.shop.booksearch.BooksSearch.dto.coupon.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BookCategoryResponse {
    private Long bookId;
    private Long primaryCategoryId;    // 1단계 (예: 800)
    private Long secondaryCategoryId;  // 2단계 (예: 810)
}