package com.daisobook.shop.booksearch.books_search.service;

import com.daisobook.shop.booksearch.books_search.exception.custom.image.MinIOServiceException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.InvalidResponseException;
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
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;

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

    @Test
    @DisplayName("getFileExtension: 쿼리 파라미터가 포함된 URL에서 확장자 추출")
    void getFileExtension_withQueryParams() throws Exception {
        // Reflection을 통해 private 메서드 직접 테스트 (또는 public 메서드 호출을 통해 검증)
        java.lang.reflect.Method method = MinIOService.class.getDeclaredMethod("getFileExtension", String.class);
        method.setAccessible(true);

        assertEquals(".png", method.invoke(minioService, "http://example.com/image.png?width=100&height=200"));
        assertEquals(".jpg", method.invoke(minioService, "http://example.com/no-extension-url"));
    }

    @Test
    @DisplayName("determineContentType: 다양한 확장자에 따른 타입 판별")
    void determineContentType_logic() throws Exception {
        java.lang.reflect.Method method = MinIOService.class.getDeclaredMethod("determineContentType", String.class);
        method.setAccessible(true);

        assertEquals("image/png", method.invoke(minioService, ".png"));
        assertEquals("image/gif", method.invoke(minioService, ".gif"));
        assertEquals("image/jpeg", method.invoke(minioService, ".bmp")); // 기본값
    }

    @Test
    @DisplayName("uploadImageFromUrl: MinIO 업로드 중 상세 예외 발생 시 MinIOServiceException 발생")
    void uploadImageFromUrl_minioPutError() throws Exception {
        byte[] data = "test".getBytes();
        when(responseSpec.bodyToMono(byte[].class)).thenReturn(Mono.just(data));

        // minioClient.putObject가 에러를 던지도록 설정
        doThrow(new RuntimeException("MinIO Internal Error"))
                .when(minioClient).putObject(any(PutObjectArgs.class));

        assertThrows(com.daisobook.shop.booksearch.books_search.exception.custom.image.MinIOServiceException.class,
                () -> minioService.uploadImageFromUrl("http://test.com/a.jpg", 1L));
    }

    @Test
    @DisplayName("uploadImageFromFile: IOException 발생 시 처리")
    void uploadImageFromFile_ioException() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getInputStream()).thenThrow(new IOException("Stream closed"));

        assertThrows(com.daisobook.shop.booksearch.books_search.exception.custom.image.MinIOServiceException.class,
                () -> minioService.uploadImageFromFile(file, 1L));
    }

    @Test
    @DisplayName("uploadImageFromUrl: 인터럽트 발생 시 상태 복구 및 예외 발생")
    void uploadImageFromUrl_interrupted() throws Exception {
        // 1. Mono.toFuture().get() 호출 시 InterruptedException을 던지도록 모킹
        // Mono를 직접 모킹하여 toFuture()가 예외를 던지는 가짜 Future를 반환하게 합니다.
        Mono<byte[]> mockMono = mock(Mono.class);
        CompletableFuture<byte[]> mockFuture = mock(CompletableFuture.class);

        when(responseSpec.bodyToMono(byte[].class)).thenReturn(mockMono);
        when(mockMono.toFuture()).thenReturn(mockFuture);

        // get() 호출 시 InterruptedException 발생 시뮬레이션
        when(mockFuture.get()).thenThrow(new InterruptedException("테스트용 인터럽트"));

        // 2. 실행 및 검증
        assertThrows(MinIOServiceException.class,
                () -> minioService.uploadImageFromUrl("http://test.com/a.jpg", 1L));

        // 3. 인터럽트 상태 복구 확인 (중요)
        // Thread.interrupted()는 상태를 확인하고 동시에 초기화(clear)하므로 테스트 마지막에 확인
        assertTrue(Thread.interrupted(), "인터럽트 상태가 복구되어야 합니다.");
    }

    @Test
    @DisplayName("deleteObject: 알 수 없는 예외 발생 시 처리")
    void deleteObject_generalException() throws Exception {
        doThrow(new RuntimeException("Unknown error")).when(minioClient).removeObject(any());

        assertThrows(com.daisobook.shop.booksearch.books_search.exception.custom.image.MinIOServiceException.class,
                () -> minioService.deleteObject("test-path"));
    }

    @Test
    @DisplayName("putObjectByUrl: MinIO 업로드 중 체크 예외 발생 시 MinIOServiceException 래핑 확인")
    void putObjectByUrl_minioCheckedException() throws Exception {
        // MinioClient가 체크 예외를 던지도록 설정
        doThrow(new InvalidResponseException(500, "text/xml", "error", "testTrace"))
                .when(minioClient).putObject(any(PutObjectArgs.class));

        byte[] data = "test".getBytes();
        when(responseSpec.bodyToMono(byte[].class)).thenReturn(Mono.just(data));

        assertThrows(MinIOServiceException.class,
                () -> minioService.uploadImageFromUrl("http://test.com/a.jpg", 1L));
    }
}
