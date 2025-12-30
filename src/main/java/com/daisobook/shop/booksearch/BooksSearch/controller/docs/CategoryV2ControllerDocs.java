package com.daisobook.shop.booksearch.BooksSearch.controller.docs;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.category.CategoryModifyReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.category.CategoryRegisterReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.category.CategoryList;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.category.CategoryTreeListRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "카테고리 V2 API", description = "도서 카테고리 목록 조회, 등록, 수정, 삭제 기능을 제공합니다.")
public interface CategoryV2ControllerDocs {

    @Operation(summary = "전체 카테고리 단순 목록 조회", description = "계층 구조 없이 모든 카테고리를 평면적인 리스트 형태로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    CategoryList getAllCategoryList();

    @Operation(summary = "카테고리 계층 구조(Tree) 조회", description = "대분류, 중분류 등 부모-자식 관계가 포함된 트리 구조의 카테고리 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    CategoryTreeListRespDTO getCategoryTreeList();

    @Operation(summary = "새 카테고리 등록", description = "새로운 도서 카테고리를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력 값")
    })
    ResponseEntity postCategory(
            @RequestBody CategoryRegisterReqDTO categoryRegisterReqDTO);

    @Operation(summary = "기존 카테고리 수정", description = "특정 카테고리의 이름이나 설정을 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리 ID")
    })
    ResponseEntity modifyCategory(
            @Parameter(description = "수정할 카테고리 ID", required = true) long categoryId,
            @RequestBody CategoryModifyReqDTO categoryModifyReqDTO);

    @Operation(summary = "카테고리 삭제", description = "특정 카테고리를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "409", description = "하위 카테고리나 연결된 도서가 있어 삭제 불가")
    })
    ResponseEntity deleteCategory(
            @Parameter(description = "삭제할 카테고리 ID", required = true) long categoryId);
}