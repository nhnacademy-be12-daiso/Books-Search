package com.daisobook.shop.booksearch.BooksSearch.mapper;

import com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookReviewProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.ImageRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.ReviewRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.order.BookReviewResponse;
import com.daisobook.shop.booksearch.BooksSearch.entity.ImageType;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.review.Review;
import com.daisobook.shop.booksearch.BooksSearch.mapper.image.ImageMapper;
import com.daisobook.shop.booksearch.BooksSearch.mapper.review.impl.ReviewMapperImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewMapperImplTest {

    @Mock
    private ImageMapper imageMapper;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ReviewMapperImpl reviewMapper;

    @Test
    @DisplayName("Review 엔티티 리스트를 ReviewRespDTO 리스트로 변환할 때 이미지 매퍼와 연동 확인")
    void toReviewRespDTOList_FromEntities_Test() {
        // Given
        Long reviewId = 1L;
        Book book = mock(Book.class);
        when(book.getId()).thenReturn(100L);
        when(book.getTitle()).thenReturn("테스트 도서");

        Review review = mock(Review.class);
        when(review.getId()).thenReturn(reviewId);
        when(review.getBook()).thenReturn(book);
        when(review.getUserId()).thenReturn(1L);
        when(review.getContent()).thenReturn("좋은 책입니다.");
        when(review.getRating()).thenReturn(5);
        when(review.getCreatedAt()).thenReturn(ZonedDateTime.now());

        // ImageMapper가 해당 리뷰의 이미지 리스트를 반환하도록 설정
        List<ImageRespDTO> images = List.of(new ImageRespDTO(1, "path/to/img.jpg", ImageType.REVIEW));
        when(imageMapper.toImagesRespDTOListMap(anyList())).thenReturn(Map.of(reviewId, images));

        // When
        List<ReviewRespDTO> result = reviewMapper.toReviewRespDTOList(List.of(review));

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(reviewId);
        assertThat(result.getFirst().reviewImages()).hasSize(1);
        assertThat(result.getFirst().reviewImages().getFirst().path()).isEqualTo("path/to/img.jpg");
    }

    @Test
    @DisplayName("BookReviewProjection 리스트를 BookReviewResponse 리스트로 변환한다")
    void toBookReviewResponseList_Test() throws JsonProcessingException {
        // Given
        BookReviewProjection projection = mock(BookReviewProjection.class);
        when(projection.getBookId()).thenReturn(200L);
        when(projection.getTitle()).thenReturn("도서 제목");
        when(projection.getImages()).thenReturn("images-json");
        when(projection.getOrderDetailId()).thenReturn(50L);
        when(projection.getReviewId()).thenReturn(10L);

        List<ImageRespDTO> images = List.of(new ImageRespDTO(1, "cover.jpg", ImageType.COVER));
        when(imageMapper.toImageRespDTOList(anyString())).thenReturn(images);

        // When
        List<BookReviewResponse> result = reviewMapper.toBookReviewResponseList(List.of(projection));

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().book().bookId()).isEqualTo(200L);
        assertThat(result.getFirst().reviewId()).isEqualTo(10L);
        assertThat(result.getFirst().book().imageList()).hasSize(1);
    }

    @Test
    @DisplayName("JSON 문자열이 비어있으면 null을 반환한다")
    void toReviewRespDTOList_Json_Empty_Test() throws JsonProcessingException {
        assertThat(reviewMapper.toReviewRespDTOList(List.of())).isNull();
        assertThat(reviewMapper.toReviewRespDTOList("  ")).isNull();
    }
}