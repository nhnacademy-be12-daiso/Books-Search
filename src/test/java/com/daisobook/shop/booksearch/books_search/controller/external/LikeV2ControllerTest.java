package com.daisobook.shop.booksearch.books_search.controller.external;

import com.daisobook.shop.booksearch.books_search.dto.response.like.MyLikeList;
import com.daisobook.shop.booksearch.books_search.service.like.impl.LikeFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LikeV2Controller.class)
class LikeV2ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LikeFacade likeFacade;

    @Test
    @DisplayName("특정 도서에 좋아요를 추가하면 201 Created를 응답한다")
    void addLike_Test() throws Exception {
        // Given
        long bookId = 1L;
        long userId = 100L;
        // void 메서드 모킹
        doNothing().when(likeFacade).addLike(bookId, userId);

        // When & Then
        mockMvc.perform(post("/api/v2/books/{bookId}/likes", bookId)
                        .header("X-User-Id", userId))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("나의 좋아요 목록을 조회하면 200 OK를 응답한다")
    void getMyLikeList_Test() throws Exception {
        // Given
        long userId = 100L;
        when(likeFacade.getMyLikeList(userId)).thenReturn(new MyLikeList(List.of()));

        // When & Then
        mockMvc.perform(get("/api/v2/likes/me")
                        .header("X-User-Id", userId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("좋아요를 취소하면 204 No Content를 응답한다")
    void deleteLike_Test() throws Exception {
        // Given
        long bookId = 1L;
        long userId = 100L;
        doNothing().when(likeFacade).deleteLike(bookId, userId);

        // When & Then
        mockMvc.perform(delete("/api/v2/books/{bookId}/likes", bookId)
                        .header("X-User-Id", userId))
                .andExpect(status().isNoContent());
    }
}