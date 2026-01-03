package com.daisobook.shop.booksearch.BooksSearch.service.image;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.ImageMetadataReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.service.ImagesReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.ImageType;
import com.daisobook.shop.booksearch.BooksSearch.entity.review.Review;
import com.daisobook.shop.booksearch.BooksSearch.entity.review.ReviewImage;
import com.daisobook.shop.booksearch.BooksSearch.repository.review.ReviewImageRepository;
import com.daisobook.shop.booksearch.BooksSearch.service.image.impl.ReviewImageServiceImpl;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReviewImageServiceTest {

    @Mock
    MinioClient minioClient;

    @Mock
    WebClient.Builder webClientBuilder;

    @Mock
    ReviewImageRepository reviewImageRepository;

    @Spy
    @InjectMocks
    ReviewImageServiceImpl service;

    private static ImageType sampleType() {
        ImageType[] consts = ImageType.class.getEnumConstants();
        if (consts != null && consts.length > 0) return consts[0];
        return null; // 거의 불가능(열거형은 최소 하나 이상 상수 가짐)
    }

    @BeforeEach
    void setup() throws Exception {
        Field f = ReviewImageServiceImpl.class.getDeclaredField("bucketName");
        f.setAccessible(true);
        f.set(service, "test-review-bucket");
    }

    @Test
    @DisplayName("addReviewImage: 이미지 저장 후 반환된 이미지 리스트 검증")
    void addReviewImage_savesReturnedImages() {
        ImagesReqDTO imagesReqDTO = mock(ImagesReqDTO.class);
        ImageMetadataReqDTO meta = mock(ImageMetadataReqDTO.class);
        when(imagesReqDTO.imageMetadata()).thenReturn(List.of(meta));
        when(meta.sequence()).thenReturn(0);
        when(meta.type()).thenReturn(sampleType());

        Map<Integer, String> urlMap = Map.of(0, "http://example.com/img0.jpg");
        doReturn(urlMap).when(service).createdProcess(eq(imagesReqDTO), nullable(Map.class));

        List<ReviewImage> result = service.addReviewImage(imagesReqDTO, Collections.emptyMap());

        ArgumentCaptor<List<ReviewImage>> captor = ArgumentCaptor.forClass((Class) List.class);
        verify(reviewImageRepository, times(1)).saveAll(captor.capture());
        List<ReviewImage> saved = captor.getValue();
        assertEquals(1, saved.size(), "저장된 이미지 수가 1개여야 한다.");
        assertEquals(1, result.size(), "반환된 리스트 크기 확인");
        assertEquals("http://example.com/img0.jpg", saved.get(0).getPath());
    }

    @Test
    @DisplayName("addReviewImages: 여러 요청 처리 및 모든 이미지 저장 검증")
    void addReviewImages_handlesMultipleRequestsAndSavesAll() {
        ImagesReqDTO req1 = mock(ImagesReqDTO.class);
        ImagesReqDTO req2 = mock(ImagesReqDTO.class);
        ImageMetadataReqDTO m1 = mock(ImageMetadataReqDTO.class);
        ImageMetadataReqDTO m2 = mock(ImageMetadataReqDTO.class);
        when(req1.imageMetadata()).thenReturn(List.of(m1));
        when(req2.imageMetadata()).thenReturn(List.of(m2));
        when(m1.sequence()).thenReturn(0);
        when(m2.sequence()).thenReturn(1);
        when(m1.type()).thenReturn(sampleType());
        when(m2.type()).thenReturn(sampleType());

        // 요청별 반환을 명확히 지정. nullable로 null 호출 허용.
        doAnswer(invocation -> {
            ImagesReqDTO arg = invocation.getArgument(0);
            if (arg == req1) return Map.of(0, "u1");
            if (arg == req2) return Map.of(1, "u2");
            return Collections.emptyMap();
        }).when(service).createdProcess(any(ImagesReqDTO.class), nullable(Map.class));

        List<List<ReviewImage>> result = service.addReviewImages(List.of(req1, req2));

        ArgumentCaptor<List<ReviewImage>> captor = ArgumentCaptor.forClass((Class) List.class);
        verify(reviewImageRepository, times(1)).saveAll(captor.capture());
        List<ReviewImage> savedAll = captor.getValue();
        assertEquals(2, savedAll.size(), "모든 요청에 대한 이미지가 저장되어야 한다.");
        assertEquals(2, result.stream().mapToInt(List::size).sum());
    }

    @Test
    @DisplayName("addReviewImage: 메타데이터가 너무 많으면 예외 발생")
    void addReviewImage_throwsIfTooManyMetadata() {
        ImagesReqDTO req = mock(ImagesReqDTO.class);
        ImageMetadataReqDTO a = mock(ImageMetadataReqDTO.class);
        ImageMetadataReqDTO b = mock(ImageMetadataReqDTO.class);
        ImageMetadataReqDTO c = mock(ImageMetadataReqDTO.class);
        ImageMetadataReqDTO d = mock(ImageMetadataReqDTO.class);
        when(req.imageMetadata()).thenReturn(List.of(a,b,c,d));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.addReviewImage(req, null));
        assertTrue(ex.getMessage().contains("최대") || !ex.getMessage().isEmpty(), "예외 메시지 존재 확인");
    }

    @Test
    @DisplayName("updateReviewImage: 기존 이미지 업데이트, 삭제, 신규 생성 검증")
    void updateReviewImage_createsUpdatesAndDeletesCorrectly() {
        ImagesReqDTO imagesReqDTO = mock(ImagesReqDTO.class);
        when(imagesReqDTO.connectedId()).thenReturn(10L);

        ImageMetadataReqDTO meta0 = mock(ImageMetadataReqDTO.class);
        ImageMetadataReqDTO meta1 = mock(ImageMetadataReqDTO.class);
        ImageMetadataReqDTO meta2 = mock(ImageMetadataReqDTO.class);
        when(meta0.sequence()).thenReturn(0);
        when(meta1.sequence()).thenReturn(1);
        when(meta2.sequence()).thenReturn(2);
        when(meta0.type()).thenReturn(sampleType());
        when(meta1.type()).thenReturn(sampleType());
        when(meta2.type()).thenReturn(sampleType());
        when(imagesReqDTO.imageMetadata()).thenReturn(List.of(meta0, meta1, meta2));

        ReviewImage pre0 = mock(ReviewImage.class);
        ReviewImage pre1 = mock(ReviewImage.class);

        Review r0mock = mock(Review.class);
        Review r1mock = mock(Review.class);
        when(r0mock.getId()).thenReturn(10L);
        when(r1mock.getId()).thenReturn(10L);
        when(pre0.getReview()).thenReturn(r0mock);
        when(pre1.getReview()).thenReturn(r1mock);

        when(pre0.getNo()).thenReturn(0);
        when(pre0.getPath()).thenReturn("old0");
        when(pre1.getNo()).thenReturn(1);
        when(pre1.getPath()).thenReturn("old1");

        when(reviewImageRepository.findReviewImagesByReview_Id(10L)).thenReturn(List.of(pre0, pre1));

        Map<Integer, String> updatedMap = new HashMap<>();
        updatedMap.put(0, "new0");
        updatedMap.put(2, "new2-url");
        // lenient로 마킹하여 "사용되지 않은 스텁" 검사에서 제외
        lenient().doReturn(updatedMap).when(service).updatedProcess(any(), anyMap(), anyList());

        // 이 스텁도 lenient 처리
        lenient().doReturn("uploaded2-url").when(service).uploadImageFromUrl("new2-url", 10L);

        List<ReviewImage> result = service.updateReviewImage(imagesReqDTO, Collections.emptyMap());

        verify(pre0, times(1)).setPath("new0");
        verify(service, times(1)).deleteObject("old1");
        verify(service, times(1)).uploadImageFromUrl("new2-url", 10L);

        ArgumentCaptor<List<ReviewImage>> delCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(reviewImageRepository, times(1)).deleteAll(delCaptor.capture());
        List<ReviewImage> deleted = delCaptor.getValue();
        assertTrue(deleted.contains(pre1), "삭제 대상에 pre1이 포함되어야 한다.");

        ArgumentCaptor<List<ReviewImage>> saveCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(reviewImageRepository, times(1)).saveAll(saveCaptor.capture());
        List<ReviewImage> saved = saveCaptor.getValue();
        assertTrue(saved.stream().anyMatch(ri -> ri == pre0), "업데이트된 기존 이미지가 저장되어야 한다.");
        assertTrue(saved.stream().anyMatch(ri -> "uploaded2-url".equals(ri.getPath())), "새로 업로드된 이미지가 저장되어야 한다.");
    }

}
