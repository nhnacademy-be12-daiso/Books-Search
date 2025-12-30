package com.daisobook.shop.booksearch.BooksSearch.controller.docs;

import com.daisobook.shop.booksearch.BooksSearch.dto.response.like.MyLikeList;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "좋아요 V2 API", description = "도서 좋아요 등록, 취소 및 내 좋아요 목록 조회 기능을 제공합니다.")
public interface LikeV2ControllerDocs {

    @Operation(summary = "도서 좋아요 등록", description = "특정 도서에 좋아요를 표시합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "좋아요 등록 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 도서 ID")
    })
    ResponseEntity addLike(
            @Parameter(description = "도서 고유 ID", required = true) long bookId,
            @Parameter(name = "X-User-Id", description = "사용자 고유 ID", required = true, in = ParameterIn.HEADER) long userId);

    @Operation(summary = "내 좋아요 목록 조회", description = "현재 로그인한 사용자가 좋아요를 누른 도서 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    MyLikeList getMyLikeList(
            @Parameter(name = "X-User-Id", description = "사용자 고유 ID", required = true, in = ParameterIn.HEADER) long userId);

    @Operation(summary = "도서 좋아요 취소", description = "기존에 표시했던 도서의 좋아요를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "좋아요 취소 성공"),
            @ApiResponse(responseCode = "404", description = "좋아요 기록을 찾을 수 없음")
    })
    ResponseEntity deleteLike(
            @Parameter(description = "도서 고유 ID", required = true) long bookId,
            @Parameter(name = "X-User-Id", description = "사용자 고유 ID", required = true, in = ParameterIn.HEADER) long userId);
}