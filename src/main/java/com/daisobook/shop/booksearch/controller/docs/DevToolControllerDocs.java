package com.daisobook.shop.booksearch.controller.docs;

import com.daisobook.shop.booksearch.dto.test.TestReq;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "System Dev Tools", description = "시스템 관리, 이미지 마이그레이션 및 테스트용 API")
public interface DevToolControllerDocs {

    @Operation(summary = "도서 생성 통합 테스트", description = "Multipart Form 데이터를 사용하여 도서 메타데이터와 이미지를 한 번에 등록 테스트합니다.")
    @ApiResponse(responseCode = "201", description = "테스트 등록 성공")
    ResponseEntity<Void> createBook(TestReq testReq) throws JsonProcessingException;

    @Operation(summary = "외부 URL 이미지 MinIO 업로드 테스트", description = "이미지 URL을 통해 MinIO 스토리지로 직접 업로드를 시도합니다.")
    ResponseEntity<String> uploadTest(Long bookId, String imageUrl);

    @Operation(summary = "MinIO 객체 삭제 테스트")
    ResponseEntity<String> deleteTest(String objectName);

    @Operation(summary = "이미지 일괄 마이그레이션 실행", description = "기존 서버의 이미지들을 배치를 통해 MinIO로 이전합니다.")
    String migrate();
}