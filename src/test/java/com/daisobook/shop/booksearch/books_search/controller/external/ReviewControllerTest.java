package com.daisobook.shop.booksearch.books_search.controller.external;

import com.daisobook.shop.booksearch.books_search.dto.point.PointPolicyType;
import com.daisobook.shop.booksearch.books_search.dto.request.review.ReviewGroupReqDTO;
import com.daisobook.shop.booksearch.books_search.dto.request.review.ReviewReqDTO;
import com.daisobook.shop.booksearch.books_search.service.book.impl.BookFacade;
import com.daisobook.shop.booksearch.books_search.service.point.PointService;
import com.daisobook.shop.booksearch.books_search.service.review.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private BookFacade bookFacade;

    @MockitoBean
    private PointService pointService;

    @Test
    @DisplayName("리뷰를 등록하면 도서에 리뷰가 등록되고 포인트 적립을 요청한다")
    void addReview_Test() throws Exception {
        // Given
        String metadata = "{\"content\":\"좋은 책입니다.\", \"userId\":100}";
        MockMultipartFile metadataPart = new MockMultipartFile("metadata", "", "application/json", metadata.getBytes());
        
        ReviewReqDTO reviewReqDTO = mock(ReviewReqDTO.class);
        when(reviewReqDTO.userId()).thenReturn(100L);
        ReviewGroupReqDTO groupDto = new ReviewGroupReqDTO(reviewReqDTO, new HashMap<>());
        
        // 1. 파싱 서비스 모킹
        when(reviewService.parsing2(anyString(), any(), any(), any())).thenReturn(groupDto);
        // 2. 파사드에서 리뷰 등록 후 포인트 타입 반환 모킹
        when(bookFacade.registerReview(any(), any())).thenReturn(PointPolicyType.REVIEW_PHOTO);
        // 3. 포인트 서비스 호출 확인을 위한 설정
        doNothing().when(pointService).requestReviewPoint(eq(100L), any(PointPolicyType.class));

        // When & Then
        mockMvc.perform(multipart("/api/reviews")
                        .file(metadataPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());

        // 로직 순서 및 호출 검증
        verify(reviewService).parsing2(anyString(), any(), any(), any());
        verify(bookFacade).registerReview(any(), any());
        verify(pointService).requestReviewPoint(eq(100L), eq(PointPolicyType.REVIEW_PHOTO));
    }

    @Test
    @DisplayName("내가 작성한 리뷰 목록을 조회한다")
    void getReviewByUserId_Test() throws Exception {
        long userId = 100L;
        when(reviewService.getReviewsByUserId(userId)).thenReturn(List.of());

        mockMvc.perform(get("/api/reviews/me")
                        .header("X-User-Id", userId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("도서별 리뷰 목록을 조회한다")
    void getReviewByBookId_Test() throws Exception {
        mockMvc.perform(get("/api/reviews/books/{bookId}", 1L)
                        .param("bookId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("리뷰를 수정하면 204 No Content를 응답한다")
    void updateReviewById_Test() throws Exception {
        // Given
        long reviewId = 50L;
        String metadata = "{\"content\":\"내용 수정\"}";
        MockMultipartFile metadataPart = new MockMultipartFile("metadata", "", "application/json", metadata.getBytes());
        
        ReviewGroupReqDTO groupDto = new ReviewGroupReqDTO(mock(ReviewReqDTO.class), new HashMap<>());
        when(reviewService.parsing2(anyString(), any(), any(), any())).thenReturn(groupDto);

        // When & Then
        mockMvc.perform(multipart("/api/reviews/{reviewId}", reviewId)
                        .file(metadataPart)
                        .with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isNoContent());
    }
}