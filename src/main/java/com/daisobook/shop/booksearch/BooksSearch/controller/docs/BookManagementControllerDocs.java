package com.daisobook.shop.booksearch.BooksSearch.controller.docs;

import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.MainPageBookListRespDTO;
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
import org.springframework.http.ResponseEntity;

@Tag(name = "Book Management Admin", description = "관리자용 도서 관리 및 메타데이터 API")
public interface BookManagementControllerDocs {

    @Operation(summary = "관리자 도서 목록 페이지 정보 조회", description = "도서 관리 테이블에 필요한 데이터와 페이징 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    AdminBookMetaData getBookAdminPageInfo(Pageable pageable);

    @Operation(summary = "도서 등록용 기초 데이터 조회", description = "도서 등록 시 선택해야 하는 카테고리, 출판사 리스트 등 기초 데이터를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    RegisterBookMetaData getBookRegisterPageInfo();

    @Operation(summary = "도서 수정용 상세 정보 조회", description = "특정 도서의 기존 정보와 수정 시 필요한 선택지 데이터를 함께 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "수정할 도서 정보를 찾을 수 없음")
    })
    ModifyBookMetaData getBookModifyPageInfo(long bookId);

    @Operation(summary = "ISBN 기반 도서 검색", description = "외부 API 혹은 기존 DB에서 ISBN으로 도서 정보를 사전 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "ISBN 형식이 올바르지 않음"),
            @ApiResponse(responseCode = "404", description = "해당 ISBN으로 검색된 도서 정보가 없음")
    })
    FindIsbnMetaData getBookRegisterRedirectSearchInfo(String isbn);

    @Operation(summary = "메인 페이지 도서 목록 조회", description = "사용자 메인 화면에 노출될 도서 목록을 조회합니다. 사용자 ID 전달 시 개인화된 정보를 포함할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    MainPageBookListRespDTO getMainPageBookList(
            @Parameter(description = "페이지 번호, 크기, 정렬 정보") Pageable pageable,
            @Parameter(description = "사용자 식별 ID (선택 사항)", example = "1") Long userId
    );
}