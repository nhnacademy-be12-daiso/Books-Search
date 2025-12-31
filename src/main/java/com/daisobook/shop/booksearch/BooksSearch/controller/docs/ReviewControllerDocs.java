package com.daisobook.shop.booksearch.BooksSearch.controller.docs;

import com.daisobook.shop.booksearch.BooksSearch.dto.response.ReviewRespDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Review V2", description = "리뷰 등록, 조회, 수정 API")
public interface ReviewControllerDocs {

    @Operation(summary = "리뷰 등록", description = "도서 리뷰를 등록하고 포인트 적립을 요청합니다. 이미지 최대 3장까지 지원합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "리뷰 등록 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
        @ApiResponse(responseCode = "404", description = "도서를 찾을 수 없음")
    })
    ResponseEntity<Void> addReview(
        @Parameter(description = "리뷰 데이터 (JSON 문자열)", required = true) String metadataJson,
        @Parameter(description = "리뷰 이미지 1") MultipartFile image0,
        @Parameter(description = "리뷰 이미지 2") MultipartFile image1,
        @Parameter(description = "리뷰 이미지 3") MultipartFile image2
    ) throws JsonProcessingException;

    @Operation(summary = "내 리뷰 목록 조회", description = "로그인한 사용자가 작성한 모든 리뷰를 조회합니다.")
    List<ReviewRespDTO> getReviewByUserId(
        @Parameter(description = "사용자 고유 ID", required = true) long userId
    );

    @Operation(summary = "도서별 리뷰 조회", description = "특정 도서에 달린 모든 리뷰를 조회합니다.")
    List<ReviewRespDTO> getReviewByBookId(
        @Parameter(description = "도서 고유 ID", required = true) long bookId
    );

    @Operation(summary = "리뷰 상세 조회", description = "리뷰 ID를 통해 특정 리뷰의 상세 정보를 조회합니다.")
    ReviewRespDTO getReviewById(
        @Parameter(description = "리뷰 고유 ID", required = true) long reviewId
    );

    @Operation(summary = "리뷰 수정", description = "기존에 작성한 리뷰 내용 및 이미지를 수정합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "리뷰 수정 성공 (반환 데이터 없음)"),
        @ApiResponse(responseCode = "403", description = "수정 권한 없음"),
        @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")
    })
    ResponseEntity<Void> updateReviewById(
        @Parameter(description = "수정할 리뷰 ID", required = true) long reviewId,
        @Parameter(description = "수정할 리뷰 데이터 (JSON 문자열)", required = true) String metadataJson,
        @Parameter(description = "수정할 이미지 1") MultipartFile image0,
        @Parameter(description = "수정할 이미지 2") MultipartFile image1,
        @Parameter(description = "수정할 이미지 3") MultipartFile image2
    ) throws JsonProcessingException;
}