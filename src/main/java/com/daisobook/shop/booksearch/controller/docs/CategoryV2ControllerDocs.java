package com.daisobook.shop.booksearch.controller.docs;

import com.daisobook.shop.booksearch.dto.request.category.CategoryModifyReqDTO;
import com.daisobook.shop.booksearch.dto.request.category.CategoryRegisterReqDTO;
import com.daisobook.shop.booksearch.dto.response.category.CategoryList;
import com.daisobook.shop.booksearch.dto.response.category.CategoryTreeListRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Category V2", description = "도서 카테고리 관리 API (계층 구조 지원)")
public interface CategoryV2ControllerDocs {

    @Operation(summary = "전체 카테고리 목록 조회", description = "모든 카테고리를 평면적인 리스트 형태로 조회합니다.")
    CategoryList getAllCategoryList();

    @Operation(summary = "카테고리 트리 구조 조회", description = "부모-자식 관계가 포함된 트리 구조의 카테고리 목록을 조회합니다.")
    CategoryTreeListRespDTO getCategoryTreeList();

    @Operation(summary = "새 카테고리 등록", description = "새로운 도서 카테고리를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "카테고리 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력 값 (중복된 이름 등)")
    })
    ResponseEntity<Void> postCategory(CategoryRegisterReqDTO categoryRegisterReqDTO);

    @Operation(summary = "기존 카테고리 수정", description = "특정 카테고리의 정보를 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "카테고리 수정 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리 ID")
    })
    ResponseEntity<Void> modifyCategory(long categoryId, CategoryModifyReqDTO categoryModifyReqDTO);

    @Operation(summary = "카테고리 삭제", description = "특정 카테고리를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "카테고리 삭제 성공"),
            @ApiResponse(responseCode = "409", description = "하위 카테고리 존재 등 연관 데이터로 인한 삭제 불가")
    })
    ResponseEntity<Void> deleteCategory(long categoryId);
}