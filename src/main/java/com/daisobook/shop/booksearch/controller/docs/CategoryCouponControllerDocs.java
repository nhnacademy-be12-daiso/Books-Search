package com.daisobook.shop.booksearch.controller.docs;

import com.daisobook.shop.booksearch.dto.coupon.response.BookCategoryResponse;
import com.daisobook.shop.booksearch.dto.response.coupon.CategorySimpleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Category Coupon & Info", description = "쿠폰 적용 및 도서 정보 조회를 위한 카테고리 데이터 API")
public interface CategoryCouponControllerDocs {

    @Operation(summary = "카테고리 ID 리스트로 단순 정보 조회", description = "쿠폰 적용 범위 확인 등을 위해 여러 카테고리의 이름과 ID 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    List<CategorySimpleResponse> getCategoriesByIds(
            @Parameter(description = "조회할 카테고리 ID 목록", example = "1,2,3") List<Long> categoryIds
    );

    @Operation(summary = "도서의 카테고리 정보 조회", description = "특정 도서가 속한 카테고리 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 도서의 카테고리 정보를 찾을 수 없음")
    })
    BookCategoryResponse getBookCategory(
            @Parameter(description = "도서 고유 ID", required = true) Long bookId
    );
}