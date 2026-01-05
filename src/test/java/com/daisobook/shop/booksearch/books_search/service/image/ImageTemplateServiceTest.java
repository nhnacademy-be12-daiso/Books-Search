package com.daisobook.shop.booksearch.books_search.service.image;

import com.daisobook.shop.booksearch.books_search.dto.request.ImageMetadataReqDTO;
import com.daisobook.shop.booksearch.books_search.dto.service.ImageDTO;
import com.daisobook.shop.booksearch.books_search.dto.service.ImagesReqDTO;
import com.daisobook.shop.booksearch.books_search.exception.custom.image.ImageServiceException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)   // 일부 테스트에서 불필요한 상호작용이 있을 수 있어 LENIENT로 설정
class ImageTemplateServiceTest {

    @Mock
    MinioClient minioClient;

    @Mock
    WebClient.Builder webClientBuilder;

    @Mock
    WebClient webClientMock;

    @Mock
    WebClient.RequestHeadersUriSpec requestUriSpec;

    @Mock
    WebClient.RequestHeadersSpec requestSpec;

    @Mock
    WebClient.ResponseSpec responseSpec;

    static class TestImageService extends ImageTemplateServiceImpl {
        public TestImageService(MinioClient minioClient, WebClient.Builder webClientBuilder) {
            super(minioClient, webClientBuilder);
        }
        @Override
        public String getBucketName() {
            return "test-bucket";
        }
    }

    TestImageService service;

    @BeforeEach
    void setUp() throws Exception {
        // webClientBuilder.build() 호출 스텁
        when(webClientBuilder.build()).thenReturn(webClientMock);

        // 실제 객체 생성 후 spy로 감싼다 (no-arg 생성자 문제 회피)
        TestImageService real = new TestImageService(minioClient, webClientBuilder);
        service = spy(real);

        // private 필드 주입
        Field f = ImageTemplateServiceImpl.class.getDeclaredField("minioUrl");
        f.setAccessible(true);
        f.set(service, "http://minio");
    }

    @Test
    @DisplayName("Minio 업로드 - data URL 처리")
    void uploadImageFromUrl_handlesDataUrlAndUploadsToMinio() throws Exception {
        String base64 = Base64.getEncoder().encodeToString("abc".getBytes(StandardCharsets.UTF_8));
        String dataUrl = "data:image/png;base64," + base64;

        String returned = service.uploadImageFromUrl(dataUrl, 42L);

        assertNotNull(returned);
        assertTrue(returned.contains("http://minio"));
        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("Minio 업로드 - 외부 URL 처리")
    void uploadImageFromUrl_externalUrl_downloadsViaWebClientAndUploads() throws Exception {
        byte[] payload = "binary-image".getBytes(StandardCharsets.UTF_8);

        when(webClientMock.get()).thenReturn(requestUriSpec);
        when(requestUriSpec.uri(anyString())).thenReturn(requestSpec);
        when(requestSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(byte[].class)).thenReturn(Mono.just(payload));

        String externalUrl = "http://example.com/image.jpg";
        String returned = service.uploadImageFromUrl(externalUrl, 7L);

        assertNotNull(returned);
        assertTrue(returned.contains("http://minio"));
        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("Minio 업로드 - MultipartFile 처리")
    void uploadImageFromFile_uploadsStreamToMinio_andReturnsUrl() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        byte[] content = "file-bytes".getBytes(StandardCharsets.UTF_8);
        when(file.isEmpty()).thenReturn(false);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(content));
        when(file.getOriginalFilename()).thenReturn("my.jpg");
        when(file.getSize()).thenReturn((long) content.length);
        when(file.getContentType()).thenReturn("image/jpeg");

        String returned = service.uploadImageFromFile(file, 99L);

        assertNotNull(returned);
        assertTrue(returned.startsWith("http://minio/"));
        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    }

    @Test
    void updateImageFromUrl_reusesObjectName_andUploads() throws Exception {
        String objectName = "99/obj.jpg";
        String fullPath = "http://minio/test-bucket/" + objectName;

        byte[] payload = "new-binary".getBytes(StandardCharsets.UTF_8);

        when(webClientMock.get()).thenReturn(requestUriSpec);
        when(requestUriSpec.uri(anyString())).thenReturn(requestSpec);
        when(requestSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(byte[].class)).thenReturn(Mono.just(payload));

        String res = service.updateImageFromUrl("http://new/image.jpg", fullPath);
        assertNotNull(res);
        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("Minio 업데이트 - MultipartFile 처리")
    void updateImageFromFile_writesToExistingObjectName() throws Exception {
        String objectName = "9/old.png";
        String fullPath = "http://minio/test-bucket/" + objectName;

        MultipartFile newFile = mock(MultipartFile.class);
        byte[] content = "newfile".getBytes(StandardCharsets.UTF_8);
        when(newFile.isEmpty()).thenReturn(false);
        when(newFile.getInputStream()).thenReturn(new ByteArrayInputStream(content));
        when(newFile.getSize()).thenReturn((long) content.length);
        when(newFile.getContentType()).thenReturn("image/png");
        when(newFile.getOriginalFilename()).thenReturn("doesnotmatter");

        String res = service.updateImageFromFile(newFile, fullPath);
        assertNotNull(res);
        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("Minio 삭제 - 객체 경로로 삭제 호출")
    void deleteObject_callsMinioRemoveObject() throws Exception {
        String fullPath = "http://minio/test-bucket/path/to/obj.jpg";
        service.deleteObject(fullPath);
        verify(minioClient, times(1)).removeObject(any());
    }

    @Test
    @DisplayName("Minio 다중 삭제 - 여러 객체 경로로 각각 삭제 호출")
    void deleteObjects_iteratesAndCallsDeleteForEach() throws Exception{
        List<String> paths = List.of(
                "http://minio/test-bucket/a.jpg",
                "http://minio/test-bucket/b.jpg"
        );
        service.deleteObjects(paths);
        verify(minioClient, times(2)).removeObject(any());
    }

    @Test
    @DisplayName("생성 처리 - 파일 맵에 키 누락 시 예외 발생")
    void createdProcess_throwsWhenFileKeyMissing_inFileMap() {
        ImagesReqDTO imagesReqDTO = mock(ImagesReqDTO.class);
        ImageMetadataReqDTO meta = mock(ImageMetadataReqDTO.class);
        when(meta.sequence()).thenReturn(5);
        when(meta.fileKey()).thenReturn("file-key");
        when(meta.dataUrl()).thenReturn("");
        when(imagesReqDTO.imageMetadata()).thenReturn(List.of(meta));
        when(imagesReqDTO.connectedId()).thenReturn(1L);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.createdProcess(imagesReqDTO, Collections.emptyMap()));
        assertNotNull(ex);
    }

    @Test
    @DisplayName("수정 처리 - 기존 이미지 목록에 해당 시퀀스가 없으면 예외 발생")
    void updatedProcess_throwsWhenExistingImageNotFound() {
        ImagesReqDTO imagesReqDTO = mock(ImagesReqDTO.class);
        ImageMetadataReqDTO meta = mock(ImageMetadataReqDTO.class);
        when(meta.sequence()).thenReturn(0);
        when(meta.dataUrl()).thenReturn("http://new");
        when(imagesReqDTO.imageMetadata()).thenReturn(List.of(meta));

        List<ImageDTO> existing = Collections.emptyList();

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.updatedProcess(imagesReqDTO, Collections.emptyMap(), existing));
        assertNotNull(ex);
    }

    @Test
    @DisplayName("생성 처리 - 여러 메타데이터에 대해 각각 업로드 호출")
    void createdProcess_withMultipleMetadata_callsUploadsForEach() throws Exception {
        ImagesReqDTO imagesReqDTO = mock(ImagesReqDTO.class);

        ImageMetadataReqDTO m1 = mock(ImageMetadataReqDTO.class);
        ImageMetadataReqDTO m2 = mock(ImageMetadataReqDTO.class);

        when(m1.sequence()).thenReturn(0);
        when(m1.dataUrl()).thenReturn("data:image/png;base64," + Base64.getEncoder().encodeToString("a".getBytes()));
        when(m1.fileKey()).thenReturn("");
        when(m2.sequence()).thenReturn(1);
        when(m2.fileKey()).thenReturn("k1");
        when(m2.dataUrl()).thenReturn("");

        when(imagesReqDTO.imageMetadata()).thenReturn(List.of(m1, m2));
        when(imagesReqDTO.connectedId()).thenReturn(100L);

        MultipartFile mf = mock(MultipartFile.class);
        when(mf.isEmpty()).thenReturn(false);
        when(mf.getInputStream()).thenReturn(new ByteArrayInputStream("xx".getBytes()));
        when(mf.getOriginalFilename()).thenReturn("f.jpg");
        when(mf.getSize()).thenReturn(2L);
        when(mf.getContentType()).thenReturn("image/jpeg");

        Map<String, MultipartFile> fileMap = new HashMap<>();
        fileMap.put("k1", mf);

        Map<Integer, String> result = service.createdProcess(imagesReqDTO, fileMap);
        assertEquals(2, result.size());
        verify(minioClient, times(2)).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("updatedProcess - 시나리오별 복합 테스트 (URL 변경, URL 유지, 파일 업로드)")
    void updatedProcess_allScenarios() throws Exception {
        // 1. Given: 3가지 케이스의 메타데이터 준비
        ImageMetadataReqDTO m1 = mock(ImageMetadataReqDTO.class); // URL 변경 케이스
        ImageMetadataReqDTO m2 = mock(ImageMetadataReqDTO.class); // URL 유지 케이스
        ImageMetadataReqDTO m3 = mock(ImageMetadataReqDTO.class); // 파일 업로드 케이스

        // m1: 새로운 외부 URL로 변경됨 (no: 0)
        when(m1.sequence()).thenReturn(0);
        when(m1.dataUrl()).thenReturn("http://new-external.com/photo.jpg");

        // m2: 기존 URL과 동일함 (no: 1)
        when(m2.sequence()).thenReturn(1);
        String sameUrl = "http://minio/test-bucket/1/same.jpg";
        when(m2.dataUrl()).thenReturn(sameUrl);

        // m3: 새로운 파일로 업데이트 (no: 2)
        when(m3.sequence()).thenReturn(2);
        when(m3.fileKey()).thenReturn("update-file-key");

        ImagesReqDTO imagesReqDTO = mock(ImagesReqDTO.class);
        when(imagesReqDTO.imageMetadata()).thenReturn(List.of(m1, m2, m3));

        // 2. Given: 기존 이미지 목록 (ImageDTO 필드명 반영)
        List<ImageDTO> existingImages = List.of(
                new ImageDTO(101L, 1L, 0, "http://minio/test-bucket/0/old.jpg", null),
                new ImageDTO(102L, 1L, 1, sameUrl, null),
                new ImageDTO(103L, 1L, 2, "http://minio/test-bucket/2/to-be-replaced.jpg", null)
        );

        // 3. Mocking: WebClient (m1용) 및 MultipartFile (m3용)
        when(webClientMock.get()).thenReturn(requestUriSpec);
        when(requestUriSpec.uri(anyString())).thenReturn(requestSpec);
        when(requestSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(byte[].class)).thenReturn(Mono.just("new-binary".getBytes()));

        MultipartFile mf = mock(MultipartFile.class);
        when(mf.isEmpty()).thenReturn(false);
        when(mf.getInputStream()).thenReturn(new ByteArrayInputStream("file-data".getBytes()));
        when(mf.getSize()).thenReturn(9L);
        when(mf.getOriginalFilename()).thenReturn("new-file.png");

        Map<String, MultipartFile> fileMap = Map.of("update-file-key", mf);

        // 4. When: 실행
        Map<Integer, String> result = service.updatedProcess(imagesReqDTO, fileMap, existingImages);

        // 5. Then: 결과 검증
        assertEquals(3, result.size());

        // 검증 1: URL이 바뀐 경우 updateImageFromUrl 호출됨
        verify(minioClient, atLeastOnce()).putObject(argThat(args -> args.object().equals("0/old.jpg")));

        // 검증 2: URL이 같은 경우 반환값은 같고 업로드는 수행하지 않음
        assertEquals(sameUrl, result.get(1));

        // 검증 3: 파일로 업데이트한 경우 정상적으로 업로드됨
        verify(minioClient, atLeastOnce()).putObject(argThat(args -> args.object().equals("2/to-be-replaced.jpg")));
    }

    @Test
    @DisplayName("getLegalImagePath - 존재하지 않는 sequence 요청 시 예외 발생 (커버리지)")
    void getLegalImagePath_throwsException_whenSequenceNotFound() {
        ImagesReqDTO imagesReqDTO = mock(ImagesReqDTO.class);
        ImageMetadataReqDTO meta = mock(ImageMetadataReqDTO.class);
        when(meta.sequence()).thenReturn(99); // 존재하지 않는 번호
        when(meta.dataUrl()).thenReturn("http://any.com");
        when(imagesReqDTO.imageMetadata()).thenReturn(List.of(meta));

        List<ImageDTO> existing = List.of(new ImageDTO(1L, 1L, 1, "path", null));

        Map<String, MultipartFile> emptyFileMap = Map.of(); // 미리 변수로 추출

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.updatedProcess(imagesReqDTO, emptyFileMap, existing));

        assertTrue(ex.getMessage().contains("경로를 찾을 수 없습니다"));
    }

    @Test
    @DisplayName("extractObjectNameFromPath - 다양한 경로 입력 케이스 (커버리지 저격)")
    void extractObjectNameFromPath_variousCases() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        // 1. null/empty 체크
        assertThrows(IllegalArgumentException.class, () -> service.deleteObject(null));
        assertThrows(IllegalArgumentException.class, () -> service.deleteObject(""));

        // 2. 쿼리 파라미터가 포함된 URL 처리
        String pathWithQuery = "http://minio/test-bucket/1/image.jpg?v=123";
        // spy를 통해 실제 로직이 수행되도록 함
        service.deleteObject(pathWithQuery);
        // "1/image.jpg"로 추출되어 removeObject가 호출되었는지 확인 가능
        verify(minioClient).removeObject(argThat(args -> args.object().equals("1/image.jpg")));

        // 3. Prefix가 없는 순수 ObjectName 처리
        service.deleteObject("simple/path.jpg");
        verify(minioClient).removeObject(argThat(args -> args.object().equals("simple/path.jpg")));
    }

    /* --- 1. Catch 블록 및 예외 처리 커버리지 저격 --- */

    @Test
    @DisplayName("uploadImageFromUrl: InterruptedException 발생 시 상태 복구 확인")
    void uploadImageFromUrl_interrupted_restoresState() throws Exception {
        // WebClient의 Mono.toFuture().get()이 InterruptedException을 던지도록 시뮬레이션
        Mono<byte[]> mockMono = mock(Mono.class);
        java.util.concurrent.CompletableFuture<byte[]> mockFuture = mock(java.util.concurrent.CompletableFuture.class);

        when(webClientMock.get()).thenReturn(requestUriSpec);
        when(requestUriSpec.uri(anyString())).thenReturn(requestSpec);
        when(requestSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(byte[].class)).thenReturn(mockMono);
        when(mockMono.toFuture()).thenReturn(mockFuture);

        when(mockFuture.get()).thenThrow(new InterruptedException("Interrupted!"));

        assertThrows(ImageServiceException.class, () -> service.uploadImageFromUrl("http://test.com/a.jpg", 1L));
        // 소나큐브 필수 체크: 인터럽트 상태가 다시 세팅되었는지 확인
        assertTrue(Thread.interrupted());
    }

    @Test
    @DisplayName("putObjectToMinio: MinIO 관련 예외 발생 시 ImageServiceException으로 변환 확인")
    void putObjectToMinio_throwsImageServiceException() throws Exception {
        // executeMinioPut 또는 putObjectToMinio 내부에서 호출되는 minioClient 에러 모킹
        doThrow(new IOException("Connection Reset"))
                .when(minioClient).putObject(any(PutObjectArgs.class));

        // updateImageFromFile 등을 통해 내부 private 메서드 호출 유도
        MultipartFile mf = mock(MultipartFile.class);
        when(mf.isEmpty()).thenReturn(false);
        when(mf.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));
        when(mf.getSize()).thenReturn(4L);

        assertThrows(ImageServiceException.class, () -> service.updateImageFromFile(mf, "1/old.jpg"));
    }

    /* --- 2. 생성/수정 프로세스의 엣지 케이스 (Null/Empty) --- */

    @Test
    @DisplayName("createdProcess: 메타데이터에 URL과 FileKey 둘 다 없을 경우 예외 발생")
    void createdProcess_noDataUrlAndNoFileKey_throwsException() {
        ImagesReqDTO req = mock(ImagesReqDTO.class);
        ImageMetadataReqDTO meta = mock(ImageMetadataReqDTO.class);
        when(meta.dataUrl()).thenReturn("");
        when(meta.fileKey()).thenReturn(""); // 둘 다 없음
        when(req.imageMetadata()).thenReturn(List.of(meta));

        Map<String, MultipartFile> map = Map.of();
        assertThrows(ImageServiceException.class, () -> service.createdProcess(req, map));
    }

    @Test
    @DisplayName("uploadImageFromFile: 파일이 비어있을 때 IllegalArgumentException")
    void uploadImageFromFile_emptyFile_throwsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.uploadImageFromFile(file, 1L));
    }

    /* --- 3. private 헬퍼 메서드 분기 커버리지 (Reflection 사용) --- */

    @Test
    @DisplayName("determineContentType: GIF 확장자 처리 확인")
    void determineContentType_gif_returnsGifType() throws Exception {
        java.lang.reflect.Method method = ImageTemplateServiceImpl.class.getDeclaredMethod("determineContentType", String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, ".gif");
        assertEquals("image/gif", result);
    }

    @Test
    @DisplayName("getFileExtension: 점(.)이 없는 경로에서 기본값 반환 확인")
    void getFileExtension_noDot_returnsDefault() throws Exception {
        java.lang.reflect.Method method = ImageTemplateServiceImpl.class.getDeclaredMethod("getFileExtension", String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, "filename-without-extension");
        assertEquals(".jpg", result);
    }

    @Test
    @DisplayName("extractObjectNameFromPath: 유효하지 않은 결과값(Empty) 추출 시 예외")
    void extractObjectNameFromPath_emptyResult_throwsException() {
        // Prefix만 있는 경로를 넣어 결과가 빈 문자열이 되도록 유도
        String prefixOnly = "http://minio/test-bucket/";
        assertThrows(IllegalArgumentException.class, () -> service.deleteObject(prefixOnly));
    }

    /* --- 4. 다중 삭제 성공 케이스 보강 --- */
    @Test
    @DisplayName("deleteObjects: 빈 리스트 입력 시 아무 일도 일어나지 않음")
    void deleteObjects_emptyList_noInteraction() {
        service.deleteObjects(Collections.emptyList());
        verifyNoInteractions(minioClient);
    }

    @Test
    @DisplayName("updateImageFromUrl: URL이 동일할 경우 업로드를 건너뛰고 기존 URL 반환")
    void updatedProcess_sameUrl_skipsUpload() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String sameUrl = "http://minio/test-bucket/1/image.jpg";
        ImageMetadataReqDTO meta = mock(ImageMetadataReqDTO.class);
        when(meta.sequence()).thenReturn(1);
        when(meta.dataUrl()).thenReturn(sameUrl);

        ImagesReqDTO req = mock(ImagesReqDTO.class);
        when(req.imageMetadata()).thenReturn(List.of(meta));

        List<ImageDTO> existing = List.of(new ImageDTO(1L, 1L, 1, sameUrl, null));

        Map<Integer, String> result = service.updatedProcess(req, Map.of(), existing);

        assertEquals(sameUrl, result.get(1));
        // 핵심: 업로드 메서드가 호출되지 않았음을 검증
        verify(minioClient, never()).putObject(any());
    }

    @Test
    @DisplayName("extractObjectNameFromPath: 다양한 경로 패턴에서 이름 추출")
    void extractObjectName_patterns() throws Exception {
        java.lang.reflect.Method method = ImageTemplateServiceImpl.class.getDeclaredMethod("extractObjectNameFromPath", String.class);
        method.setAccessible(true);

        // 1. Full URL (Prefix 포함)
        String url = "http://minio/test-bucket/123/uuid.jpg";
        assertEquals("123/uuid.jpg", method.invoke(service, url));

        // 2. Query Parameter 포함
        String urlWithQuery = "http://minio/test-bucket/123/uuid.jpg?versionId=456";
        assertEquals("123/uuid.jpg", method.invoke(service, urlWithQuery));

        // 3. 순수 경로만 있는 경우
        String purePath = "123/uuid.jpg";
        assertEquals("123/uuid.jpg", method.invoke(service, purePath));
    }

    @Test
    @DisplayName("uploadImageFromUrl: ExecutionException 발생 시 처리")
    void uploadImageFromUrl_executionException() throws Exception {
        // WebClient 가짜 에러 유도
        when(webClientMock.get()).thenReturn(requestUriSpec);
        when(requestUriSpec.uri(anyString())).thenReturn(requestSpec);
        when(requestSpec.retrieve()).thenReturn(responseSpec);

        Mono<byte[]> mockMono = mock(Mono.class);
        java.util.concurrent.CompletableFuture<byte[]> mockFuture = mock(java.util.concurrent.CompletableFuture.class);

        when(responseSpec.bodyToMono(byte[].class)).thenReturn(mockMono);
        when(mockMono.toFuture()).thenReturn(mockFuture);

        // ExecutionException은 Checked Exception이므로 시뮬레이션
        when(mockFuture.get()).thenThrow(new java.util.concurrent.ExecutionException(new RuntimeException("Thread Error")));

        assertThrows(ImageServiceException.class, () -> service.uploadImageFromUrl("http://test.com/a.jpg", 1L));
    }

    @Test
    @DisplayName("deleteObject: MinioException 발생 시 예외 변환")
    void deleteObject_minioException() throws Exception {
        // MinioException은 추상 클래스이므로 하위 클래스 중 하나를 사용
        doThrow(new io.minio.errors.ServerException("Server Error", 500, "Error"))
                .when(minioClient).removeObject(any());

        assertThrows(ImageServiceException.class, () -> service.deleteObject("http://minio/test-bucket/1/a.jpg"));
    }
}
