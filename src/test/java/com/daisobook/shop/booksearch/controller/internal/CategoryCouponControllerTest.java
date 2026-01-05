package com.daisobook.shop.booksearch.controller.internal;

import com.daisobook.shop.booksearch.controller.internal.coupon.CategoryCouponController;
import com.daisobook.shop.booksearch.dto.coupon.response.BookCategoryResponse;
import com.daisobook.shop.booksearch.service.category.CategoryV2Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryCouponController.class)
class CategoryCouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryV2Service categoryService;

    @Test
    @DisplayName("카테고리 ID 리스트로 간단한 카테고리 정보 목록을 조회한다")
    void getCategoriesByIds_Test() throws Exception {
        // Given
        List<Long> ids = List.of(1L, 2L, 3L);
        when(categoryService.findByIdIn(anyList())).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/books/categoriesIds")
                        .param("ids", "1,2,3") // 리스트 파라미터 전달 방식
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("도서 ID로 해당 도서의 카테고리 정보를 조회한다")
    void getBookCategory_Test() throws Exception {
        // Given
        long bookId = 100L;
        when(categoryService.bookCategory(anyLong())).thenReturn(new BookCategoryResponse(1L, 11L, 111L));

        // When & Then
        mockMvc.perform(get("/api/books/{bookId}/category", bookId))
                .andExpect(status().isOk());
    }
}