package com.daisobook.shop.booksearch.BooksSearch.controller.docs;

import com.daisobook.shop.booksearch.BooksSearch.dto.response.meta.AdminBookMetaData;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.meta.FindIsbnMetaData;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.meta.ModifyBookMetaData;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.meta.RegisterBookMetaData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;

@Tag(name = "도서 관리 API", description = "관리자 페이지의 도서 조회, 등록, 수정 및 ISBN 검색 관련 API")
public interface BookManagementControllerDocs {
    @Operation(summary = "관리자 도서 목록 페이지 정보 조회", description = "관리자 페이지에서 필요한 도서 목록 메타데이터를 페이지별로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    AdminBookMetaData getBookAdminPageInfo(
            @Parameter(description = "페이지네이션 (size, sort 등)", example = "{\"size\": 15, \"sort\": \"publication_date,desc\"}") Pageable pageable);

    @Operation(summary = "도서 등록 페이지 초기 정보 조회", description = "도서 등록에 필요한 카테고리, 저자 등 기본 메타데이터를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    RegisterBookMetaData getBookRegisterPageInfo();

    @Operation(summary = "도서 수정 페이지 정보 조회", description = "특정 도서의 ID를 이용해 해당 도서의 기존 정보와 수정 가능한 메타데이터를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 도서를 찾을 수 없음")
    })
    ModifyBookMetaData getBookModifyPageInfo(
            @Parameter(description = "수정할 도서의 고유 ID", required = true) long bookId);

    @Operation(summary = "ISBN 기반 등록 리다이렉트 정보 조회", description = "ISBN을 통해 도서 정보를 검색하여 등록 페이지로 전달할 데이터를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 ISBN 형식")
    })
    FindIsbnMetaData getBookRegisterRedirectSearchInfo(
            @Parameter(description = "검색할 도서의 ISBN", required = true) String isbn);
}