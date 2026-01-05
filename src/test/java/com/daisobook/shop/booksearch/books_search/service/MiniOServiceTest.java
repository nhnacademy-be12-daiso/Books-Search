package com.daisobook.shop.booksearch.books_search.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Mono;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MiniOServiceTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private WebClient.Builder webClientBuilder;

    // WebClient chain mocks
    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestHeadersUriSpec uriSpec;
    @Mock
    private WebClient.RequestHeadersSpec headersSpec;
    @Mock
    private ResponseSpec responseSpec;

    private MinIOService minioService;

    @BeforeEach
    void setup() throws Exception {
        // build chain mocks
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);

        // create service with mocks
        minioService = new MinIOService(minioClient, webclientBuilder());

        // set private @Value fields
        setField(minioService, "bucketName", "test-bucket");
        setField(minioService, "minioUrl", "https://minio.local");
    }

    private WebClient.Builder webclientBuilder() {
        return webClientBuilder;
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field f = MinIOService.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test
    @DisplayName("uploadImageFromUrl 성공 시 MinIO에 업로드하고 URL 반환")
    void uploadImageFromUrl_success_callsMinioAndReturnsUrl() throws Exception {
        byte[] data = "image-bytes".getBytes();
        when(responseSpec.bodyToMono(byte[].class)).thenReturn(Mono.just(data));

        String result = minioService.uploadImageFromUrl("http://example.com/a.jpg", 123L);

        assertNotNull(result);
        assertTrue(result.startsWith("https://minio.local/test-bucket/123/"));
        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("uploadImageFromUrl 다운로드 오류 시 RuntimeException 발생")
    void uploadImageFromUrl_downloadError_throwsRuntime() {
        when(responseSpec.bodyToMono(byte[].class)).thenReturn(Mono.error(new RuntimeException("down")));
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> minioService.uploadImageFromUrl("http://example.com/b.png", 1L));
        assertTrue(ex.getMessage().contains("이미지 다운로드"));
    }

    @Test
    @DisplayName("uploadImageFromFile 성공 시 스트림 읽고 MinIO에 업로드")
    void uploadImageFromFile_success_readsStream_and_callsMinio() throws Exception {
        byte[] bytes = "file-data".getBytes();
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(bytes));
        when(file.getOriginalFilename()).thenReturn("orig.png");
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn((long) bytes.length);

        String url = minioService.uploadImageFromFile(file, 55L);
        assertNotNull(url);
        assertTrue(url.startsWith("https://minio.local/test-bucket/55/"));
        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("uploadImageFromFile 빈 파일 시 IllegalArgumentException 발생")
    void uploadImageFromFile_empty_throwsIllegalArgument() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> minioService.uploadImageFromFile(file, 2L));
        verify(minioClient, never()).putObject(any());
    }

    @Test
    @DisplayName("updateImageFromUrl 성공 시 기존 경로 덮어쓰고 동일 경로 반환")
    void updateImageFromUrl_success_overwritesAndReturnsSamePath() throws Exception {
        byte[] data = "new".getBytes();
        when(responseSpec.bodyToMono(byte[].class)).thenReturn(Mono.just(data));

        String existing = "55/my-file.jpg";
        String out = minioService.updateImageFromUrl("http://other/img.jpg", existing);

        assertEquals("https://minio.local/test-bucket/" + existing, out);
        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("updateImageFromUrl 다운로드 오류 시 RuntimeException 발생")
    void updateImageFromUrl_error_throwsRuntime() {
        when(responseSpec.bodyToMono(byte[].class)).thenReturn(Mono.error(new RuntimeException("nx")));
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> minioService.updateImageFromUrl("http://x/x", "1/x.jpg"));
        assertTrue(ex.getMessage().contains("이미지 업데이트"));
    }

    @Test
    @DisplayName("updateImageFromFile 성공 시 기존 경로 덮어쓰고 동일 경로 반환")
    void updateImageFromFile_success_overwritesAndReturnsUrl() throws Exception {
        byte[] bytes = "upd".getBytes();
        MultipartFile newFile = mock(MultipartFile.class);
        when(newFile.isEmpty()).thenReturn(false);
        when(newFile.getInputStream()).thenReturn(new ByteArrayInputStream(bytes));
        when(newFile.getSize()).thenReturn((long) bytes.length);
        when(newFile.getContentType()).thenReturn("image/jpeg");

        String existing = "10/existing.jpg";
        String out = minioService.updateImageFromFile(newFile, existing);
        assertEquals("https://minio.local/test-bucket/" + existing, out);
        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("updateImageFromFile 빈 파일 시 IllegalArgumentException 발생")
    void updateImageFromFile_empty_throwsIllegalArgument() throws Exception {
        MultipartFile newFile = mock(MultipartFile.class);
        when(newFile.isEmpty()).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> minioService.updateImageFromFile(newFile, "x.jpg"));
        verify(minioClient, never()).putObject(any());
    }

    @Test
    @DisplayName("deleteObject 성공 시 MinIO removeObject 호출")
    void deleteObject_success_callsRemove() throws Exception{
        doNothing().when(minioClient).removeObject(any());
        minioService.deleteObject("some/object.jpg");
        verify(minioClient, times(1)).removeObject(any());
    }

    @Test
    @DisplayName("deleteObject MinioException 시 RuntimeException으로 래핑하여 던짐")
    void deleteObject_minioException_wrappedAsRuntime() throws Exception {
        doAnswer(invocation -> {
            sneakyThrow(new MinioException("bad"));
            return null;
        }).when(minioClient).removeObject(any());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> minioService.deleteObject("bad.obj"));
        assertTrue(ex.getMessage().contains("MinIO 삭제 오류"));
    }

    // helper to throw checked exception as-is at runtime
    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow0(Throwable t) throws T {
        throw (T) t;
    }

    private static void sneakyThrow(Throwable t) {
        MiniOServiceTest.<RuntimeException>sneakyThrow0(t);
    }
}
