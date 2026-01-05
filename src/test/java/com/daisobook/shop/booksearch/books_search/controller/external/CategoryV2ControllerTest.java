package com.daisobook.shop.booksearch.books_search.controller.external;

import com.daisobook.shop.booksearch.books_search.dto.request.category.CategoryModifyReqDTO;
import com.daisobook.shop.booksearch.books_search.dto.request.category.CategoryRegisterReqDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.category.CategoryList;
import com.daisobook.shop.booksearch.books_search.dto.response.category.CategoryTreeListRespDTO;
import com.daisobook.shop.booksearch.books_search.service.category.CategoryV2Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryV2Controller.class)
class CategoryV2ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryV2Service categoryService;

    @Test
    @DisplayName("전체 카테고리 목록 조회 API 테스트")
    void getAllCategoryList_Test() throws Exception {
        // Given
        when(categoryService.getCategoryList()).thenReturn(new CategoryList(List.of()));

        // When & Then
        mockMvc.perform(get("/api/v2/books/categories"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("카테고리 트리 구조 조회 API 테스트")
    void getCategoryTreeList_Test() throws Exception {
        // Given
        when(categoryService.getCategoryTreeList()).thenReturn(new CategoryTreeListRespDTO(List.of()));

        // When & Then
        mockMvc.perform(get("/api/v2/books/categories/tree"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("카테고리 등록 API 테스트")
    void postCategory_Test() throws Exception {
        // Given
        CategoryRegisterReqDTO req = new CategoryRegisterReqDTO(1, "신규 카테고리", 1, null);
        doNothing().when(categoryService).registerCategory(any(CategoryRegisterReqDTO.class));

        // When & Then
        mockMvc.perform(post("/api/v2/books/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("카테고리 수정 API 테스트")
    void modifyCategory_Test() throws Exception {
        // Given
        long categoryId = 1L;
        CategoryModifyReqDTO req = new CategoryModifyReqDTO("수정 이름", 1);
        // void 메서드 모킹 시 올바른 문법: doNothing().when(mock).method()
        doNothing().when(categoryService).modifyCategory(anyLong(), any(CategoryModifyReqDTO.class));

        // When & Then
        mockMvc.perform(put("/api/v2/books/categories/{categoryId}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("카테고리 삭제 API 테스트")
    void deleteCategory_Test() throws Exception {
        // Given
        doNothing().when(categoryService).deleteCategory(1L);

        // When & Then
        mockMvc.perform(delete("/api/v2/books/categories/{categoryId}", 1L))
                .andExpect(status().isNoContent());
    }
}