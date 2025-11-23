package com.daisobook.shop.booksearch.BooksSearch.controller;

import com.daisobook.shop.booksearch.BooksSearch.dto.test.BookCreationRequest;
import com.daisobook.shop.booksearch.BooksSearch.dto.test.TestReq;
import com.daisobook.shop.booksearch.BooksSearch.service.TestService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
public class TestController {
    private final TestService testService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/create", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createBook(TestReq testReq) throws JsonProcessingException {
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
        return ResponseEntity.ok().build();
    }

    // 여러개의 도서를 하는거 하기 어려운건지 안된는건지 포기
//    @PostMapping(value = "/creates", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
//    public ResponseEntity<?> createBooks(ArrayList<TestReq> testReqs) throws JsonProcessingException {
//        return ResponseEntity.ok().build();
//    }

}
