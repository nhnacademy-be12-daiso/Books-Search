package com.daisobook.shop.booksearch.books_search.controller.external;

import com.daisobook.shop.booksearch.books_search.service.MinIOService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImageController.class)
class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MinIOService minIOService;

    @Test
    @DisplayName("이미지 파일을 업로드하면 프록시 경로가 포함된 URL을 반환한다")
    void uploadImage_Test() throws Exception {
        // Given
        String fileName = "test-image.jpg";
        String bucketPath = "http://minio-url.com/bucket/test-image.jpg";
        
        // 가짜 이미지 파일 생성 (name은 컨트롤러의 @RequestPart("image")와 일치해야 함)
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", 
                fileName, 
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        // 서비스가 반환할 경로 모킹
        when(minIOService.uploadImageFromFile(any(), eq(1L))).thenReturn(bucketPath);

        // When & Then
        mockMvc.perform(multipart("/api/books/images/upload")
                        .file(imageFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string("/proxy/image?url=" + bucketPath));
    }
}