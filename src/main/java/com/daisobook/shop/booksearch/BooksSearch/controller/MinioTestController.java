package com.daisobook.shop.booksearch.BooksSearch.controller;

import com.daisobook.shop.booksearch.BooksSearch.service.MinioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/minio-test")
public class MinioTestController {

    private final MinioService minioService;

    public MinioTestController(MinioService minioService) {
        this.minioService = minioService;
    }

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
}