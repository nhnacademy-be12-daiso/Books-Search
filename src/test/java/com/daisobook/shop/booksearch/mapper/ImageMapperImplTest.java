package com.daisobook.shop.booksearch.mapper;

import com.daisobook.shop.booksearch.dto.request.ImageMetadataReqDTO;
import com.daisobook.shop.booksearch.dto.response.ImageRespDTO;
import com.daisobook.shop.booksearch.dto.service.ImagesReqDTO;
import com.daisobook.shop.booksearch.entity.ImageType;
import com.daisobook.shop.booksearch.entity.book.Book;
import com.daisobook.shop.booksearch.entity.review.Review;
import com.daisobook.shop.booksearch.entity.review.ReviewImage;
import com.daisobook.shop.booksearch.mapper.image.impl.ImageMapperImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageMapperImplTest {

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ImageMapperImpl imageMapper;

    @Test
    @DisplayName("ISBN 기준의 도서 맵과 이미지 메타데이터 맵을 조합하여 ImagesReqDTO 리스트를 생성한다")
    void createImagesReqDTOListTest() {
        // Given
        String isbn = "1234567890";
        Book book = mock(Book.class);
        when(book.getId()).thenReturn(100L);
        
        Map<String, Book> bookMap = Map.of(isbn, book);
        
        ImageMetadataReqDTO meta = new ImageMetadataReqDTO(1, ImageType.COVER, "path/test.jpg", null);
        Map<String, List<ImageMetadataReqDTO>> imageListMap = Map.of(isbn, List.of(meta));

        // When
        List<ImagesReqDTO> result = imageMapper.createImagesReqDTOList(bookMap, imageListMap);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().connectedId()).isEqualTo(100L);
        assertThat(result.getFirst().imageMetadata().getFirst().dataUrl()).isEqualTo("path/test.jpg");
    }

    @Test
    @DisplayName("리뷰 리스트를 받아 리뷰 ID를 키로 하는 이미지 DTO 맵을 생성한다")
    void toImagesRespDTOListMapTest() {
        // Given
        Review review = mock(Review.class);
        when(review.getId()).thenReturn(50L);
        
        ReviewImage ri = new ReviewImage();
        ri.setNo(1);
        ri.setPath("review/img.png");
        ri.setImageType(ImageType.REVIEW);
        
        when(review.getReviewImages()).thenReturn(List.of(ri));
        List<Review> reviews = List.of(review);

        // When
        Map<Long, List<ImageRespDTO>> result = imageMapper.toImagesRespDTOListMap(reviews);

        // Then
        assertThat(result).containsKey(50L);
        assertThat(result.get(50L).getFirst().path()).isEqualTo("review/img.png");
    }

    @Test
    @DisplayName("JSON 형태의 이미지 데이터를 맵 구조로 정확히 역직렬화한다")
    void toIageRespDTOMapTest() throws JsonProcessingException {
        // Given
        String json = "[{\"no\":1,\"path\":\"img.jpg\",\"imageType\":\"COVER\"}]";
        Map<Long, String> imageDataMap = Map.of(1L, json);

        // When
        Map<Long, List<ImageRespDTO>> result = imageMapper.toIageRespDTOMap(imageDataMap);

        // Then
        assertThat(result.get(1L)).hasSize(1);
        assertThat(result.get(1L).getFirst().imageType()).isEqualTo(ImageType.COVER);
    }

    @Test
    @DisplayName("이미지 데이터 문자열이 null이거나 비어있으면 null을 반환한다")
    void toImageRespDTOList_Json_Empty_Test() throws JsonProcessingException {
        assertThat(imageMapper.toImageRespDTOList((String) null)).isNull();
        assertThat(imageMapper.toImageRespDTOList(" ")).isNull();
    }
}