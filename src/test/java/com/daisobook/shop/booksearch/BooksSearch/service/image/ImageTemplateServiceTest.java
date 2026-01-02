package com.daisobook.shop.booksearch.BooksSearch.service.image;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.ImageMetadataReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.service.ImageDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.service.ImagesReqDTO;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
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
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
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
}
