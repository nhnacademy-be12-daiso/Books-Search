package com.daisobook.shop.booksearch.controller;

import com.daisobook.shop.booksearch.dto.test.BookCreationRequest;
import com.daisobook.shop.booksearch.service.ImageMigrationService;
import com.daisobook.shop.booksearch.service.MinIOService;
import com.daisobook.shop.booksearch.service.TestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TestController.class) // 컨트롤러 계층만 가볍게 테스트
class TestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private TestService testService;
    @MockitoBean private MinIOService minioService;
    @MockitoBean private ImageMigrationService imageMigrationService;

    @Test
    @DisplayName("도서 생성 테스트 - 리플렉션을 통한 이미지 파일 추출 검증")
    void createBook_Success_Test() throws Exception {
        // 1. 준비: 메타데이터 JSON 및 가짜 파일
        String metadataJson = "{\"title\":\"테스트 도서\"}";
        MockMultipartFile image0 = new MockMultipartFile("image0", "test.jpg", "image/jpeg", "test data".getBytes());
        
        // 2. 실행 & 검증
        mockMvc.perform(multipart("/create")
                        .file(image0) // TestReq의 필드명과 일치해야 함
                        .param("metadata", metadataJson))
                .andExpect(status().isCreated());

        // 3. 서비스 호출 확인 (리플렉션으로 파일이 잘 넘어갔는지 확인)
        verify(testService).processBookCreation(any(BookCreationRequest.class), any(Map.class));
    }

    @Test
    @DisplayName("MinIO 업로드 테스트 - URL 호출 확인")
    void uploadTest_Success() throws Exception {
        mockMvc.perform(get("/upload")
                        .param("bookId", "1")
                        .param("imageUrl", "http://example.com/image.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("업로드 성공")));

        verify(minioService).uploadImageFromUrl("http://example.com/image.jpg", 1L);
    }

    @Test
    @DisplayName("이미지 마이그레이션 실행 테스트")
    void migrate_Success() throws Exception {
        mockMvc.perform(post("/migrate-images"))
                .andExpect(status().isOk())
                .andExpect(content().string("Migration process started. Check logs for details."));

        verify(imageMigrationService).migrateInBatches();
    }
}