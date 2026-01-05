package com.daisobook.shop.booksearch.service;

import com.daisobook.shop.booksearch.dto.test.BookCreationRequest;
import com.daisobook.shop.booksearch.dto.request.ImageMetadataReqDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestServiceTest {

    @MockitoBean
    @Mock
    private MinIOService minioService;

    private TestService testService;

    @BeforeEach
    void setUp() {
        testService = new TestService(minioService);
    }

    @Test
    @DisplayName("dataUrl이 존재하면 uploadImageFromUrl이 호출된다")
    void dataUrl_present_callsUploadImageFromUrl_once() {
        BookCreationRequest req = mock(BookCreationRequest.class);
        ImageMetadataReqDTO img = mock(ImageMetadataReqDTO.class);

        when(img.dataUrl()).thenReturn("http://example.com/img.jpg");
        when(req.imageMetadata()).thenReturn(List.of(img));

        testService.processBookCreation(req, Map.of());

        verify(minioService, times(1)).uploadImageFromUrl("http://example.com/img.jpg", 6L);
        verify(minioService, never()).uploadImageFromFile(any(MultipartFile.class), anyLong());
    }

    @Test
    @DisplayName("fileKey가 존재하고 해당 파일이 제공되면 uploadImageFromFile이 호출된다")
    void fileKey_present_and_file_exists_callsUploadImageFromFile_once() {
        BookCreationRequest req = mock(BookCreationRequest.class);
        ImageMetadataReqDTO img = mock(ImageMetadataReqDTO.class);
        MultipartFile file = mock(MultipartFile.class);

        when(img.dataUrl()).thenReturn("");
        when(img.fileKey()).thenReturn("file-key");
        when(req.imageMetadata()).thenReturn(List.of(img));

        testService.processBookCreation(req, Map.of("file-key", file));

        verify(minioService, times(1)).uploadImageFromFile(file, 6L);
        verify(minioService, never()).uploadImageFromUrl(anyString(), anyLong());
    }

    @Test
    @DisplayName("fileKey가 존재하지만 해당 파일이 없으면 RuntimeException이 발생한다")
    void fileKey_present_but_file_missing_throwsRuntimeException() {
        BookCreationRequest req = mock(BookCreationRequest.class);
        ImageMetadataReqDTO img = mock(ImageMetadataReqDTO.class);

        when(img.dataUrl()).thenReturn("");
        when(img.fileKey()).thenReturn("missing-key");
        when(req.imageMetadata()).thenReturn(List.of(img));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> testService.processBookCreation(req, Map.of()));
        // 메시지는 서비스에서 기본 RuntimeException이므로 존재 여부만 체크
        assertNotNull(ex);
        verifyNoInteractions(minioService);
    }

    @Test
    @DisplayName("dataUrl과 fileKey가 모두 없으면 RuntimeException이 발생한다")
    void neither_dataUrl_nor_fileKey_throwsRuntimeException() {
        BookCreationRequest req = mock(BookCreationRequest.class);
        ImageMetadataReqDTO img = mock(ImageMetadataReqDTO.class);

        when(img.dataUrl()).thenReturn("");
        when(img.fileKey()).thenReturn("");
        when(req.imageMetadata()).thenReturn(List.of(img));

        assertThrows(RuntimeException.class, () -> testService.processBookCreation(req, Map.of()));
        verifyNoInteractions(minioService);
    }

    @Test
    @DisplayName("여러 이미지 처리 - dataUrl과 fileKey 혼합, 모두 정상 처리")
    void multiple_images_mixed_types_allHandled() {
        BookCreationRequest req = mock(BookCreationRequest.class);
        ImageMetadataReqDTO img1 = mock(ImageMetadataReqDTO.class);
        ImageMetadataReqDTO img2 = mock(ImageMetadataReqDTO.class);
        MultipartFile file = mock(MultipartFile.class);

        when(img1.dataUrl()).thenReturn("http://a");

        when(img2.dataUrl()).thenReturn("");
        when(img2.fileKey()).thenReturn("fk");

        when(req.imageMetadata()).thenReturn(List.of(img1, img2));

        testService.processBookCreation(req, Map.of("fk", file));

        verify(minioService, times(1)).uploadImageFromUrl("http://a", 6L);
        verify(minioService, times(1)).uploadImageFromFile(file, 6L);
    }
}
