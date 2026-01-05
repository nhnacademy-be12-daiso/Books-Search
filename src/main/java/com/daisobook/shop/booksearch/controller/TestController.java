package com.daisobook.shop.booksearch.controller;

import com.daisobook.shop.booksearch.controller.docs.DevToolControllerDocs;
import com.daisobook.shop.booksearch.dto.test.BookCreationRequest;
import com.daisobook.shop.booksearch.dto.test.TestReq;
import com.daisobook.shop.booksearch.service.ImageMigrationService;
import com.daisobook.shop.booksearch.service.MinIOService;
import com.daisobook.shop.booksearch.service.TestService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController("apiTestController")
public class TestController implements DevToolControllerDocs {
    private final TestService testService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/create", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Void> createBook(TestReq testReq) throws JsonProcessingException {
        if(testReq == null){
            throw new RuntimeException("null");
        }

        final int MAX_FILE_COUNT = 5;

        BookCreationRequest metadata = objectMapper.readValue(testReq.getMetadata(), BookCreationRequest.class);
        Map<String, MultipartFile> files = new HashMap<>();
        Class<?> clazz = testReq.getClass();

        for(int i = 0; i < MAX_FILE_COUNT; i++) {
            String key = "image%d".formatted(i);
            try {
                // DTO에서 필드를 찾아 접근 권한 설정
                Field field = clazz.getDeclaredField(key);
                field.setAccessible(true);

                // DTO 인스턴스에서 해당 필드의 값(MultipartFile) 추출
                MultipartFile file = (MultipartFile) field.get(testReq);

                // 파일이 비어있지 않은 경우에만 Map에 추가 (Key는 "image0", "image1"...)
                if (file != null && !file.isEmpty()) {
                    files.put(key, file);
                }
            } catch (NoSuchFieldException e) {
                // 필드가 없으면 종료
                break;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        testService.processBookCreation(metadata, files);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //기존의 MinioTestController에 있던거
    private final MinIOService minioService;

    /**
     * [테스트 1] 외부 URL 이미지를 MiniIO에 업로드합니다.
     * GET /api/minio-test/upload?bookId=999&imageUrl=https://example.com/cover.jpg
     * http://localhost:8080/api/minio-test/upload?bookId=1&imageUrl=https://img.seoul.co.kr/img/upload/2017/05/08/SSI_20170508145003.jpg
     */
    @GetMapping("/upload")
    public ResponseEntity<String> uploadTest(
            @RequestParam Long bookId,
            @RequestParam String imageUrl) {

        try {
            // MinioService의 업로드 메서드 호출
            String savedUrl = minioService.uploadImageFromUrl(imageUrl, bookId);

            return ResponseEntity.ok("✅ 업로드 성공! DB에 저장할 URL: " + savedUrl);

        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("❌ 업로드 실패: " + e.getMessage());
        }
    }

    /**
     * [테스트 2] MiniIO에 저장된 파일을 삭제합니다.
     * DELETE /api/minio-test/delete?objectName=999/랜덤UUID.jpg
     * * 주의: objectName은 버킷 이름 뒤의 경로여야 합니다. (예: 999/abc-123.jpg)
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteTest(@RequestParam String objectName) {
        try {
            minioService.deleteObject(objectName);
            return ResponseEntity.ok("✅ 삭제 성공! 객체 이름: " + objectName);
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("❌ 삭제 실패: " + e.getMessage());
        }
    }

    //기존의 AdminController에 있던거
    private final ImageMigrationService imageMigrationService;

    @PostMapping("/migrate-images")
    public String migrate() {
//        imageMigrationService.migrateAllImages();
        imageMigrationService.migrateInBatches();
        return "Migration process started. Check logs for details.";
    }
}
