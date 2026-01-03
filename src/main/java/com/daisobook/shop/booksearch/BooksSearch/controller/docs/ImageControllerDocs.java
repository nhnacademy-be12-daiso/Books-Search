package com.daisobook.shop.booksearch.BooksSearch.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Image API", description = "이미지 업로드 및 관리 API")
public interface ImageControllerDocs {

    @Operation(
        summary = "이미지 단일 업로드", 
        description = "파일을 업로드하고, 프록시를 통해 접근 가능한 이미지 경로를 반환받습니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "업로드 성공 (프록시 URL 반환)",
            content = @Content(schema = @Schema(type = "string", example = "/proxy/image?url=http://minio-url/bucket/image.jpg"))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 파일 형식 또는 업로드 실패"),
        @ApiResponse(responseCode = "500", description = "서버 내부 이미지 처리 오류")
    })
    String upload(
        @Parameter(
            description = "업로드할 이미지 파일 (MultipartFile)", 
            required = true
        ) MultipartFile image
    );
}