package com.daisobook.shop.booksearch.controller.external;

import com.daisobook.shop.booksearch.dto.response.book.MainPageBookListRespDTO;
import com.daisobook.shop.booksearch.dto.response.meta.AdminBookMetaData;
import com.daisobook.shop.booksearch.dto.response.meta.FindIsbnMetaData;
import com.daisobook.shop.booksearch.dto.response.meta.ModifyBookMetaData;
import com.daisobook.shop.booksearch.dto.response.meta.RegisterBookMetaData;
import com.daisobook.shop.booksearch.service.meta.MetadataService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookManagementController.class)
class BookManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MetadataService metadataService;

    @Test
    @DisplayName("관리자 도서 목록 메타데이터 조회 API 테스트")
    void getBookAdminPageInfo_Test() throws Exception {
        // Given
        AdminBookMetaData mockData = new AdminBookMetaData(null, null); // 실제 DTO 구조에 맞게 초기화
        when(metadataService.getAdminBookMataData(any(Pageable.class))).thenReturn(mockData);

        // When & Then
        mockMvc.perform(get("/api/v2/books/admin")
                        .param("page", "0")
                        .param("size", "15"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("도서 등록 기초 데이터 조회 API 테스트")
    void getBookRegisterPageInfo_Test() throws Exception {
        // Given
        RegisterBookMetaData mockData = new RegisterBookMetaData(null, null);
        when(metadataService.getRegisterBookMataDataFromAdmin()).thenReturn(mockData);

        // When & Then
        mockMvc.perform(get("/api/v2/books/metadata/registration"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("특정 도서 수정용 상세 데이터 조회 API 테스트")
    void getBookModifyPageInfo_Test() throws Exception {
        // Given
        long bookId = 1L;
        ModifyBookMetaData mockData = new ModifyBookMetaData(null, null, null);
        when(metadataService.getModifyBookMataDataFromAdmin(bookId)).thenReturn(mockData);

        // When & Then
        mockMvc.perform(get("/api/v2/books/{bookId}/metadata/modification", bookId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ISBN 검색을 통한 등록 정보 조회 API 테스트")
    void getBookRegisterRedirectSearchInfo_Test() throws Exception {
        // Given
        String isbn = "1234567890123";
        FindIsbnMetaData mockData = new FindIsbnMetaData(null, null, null);
        when(metadataService.getFindIsbnMataDataFromAdmin(isbn)).thenReturn(mockData);

        // When & Then
        mockMvc.perform(get("/api/v2/books/{isbn}/register-page", isbn))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("메인 페이지 도서 목록 조회 API 테스트 (Header 포함)")
    void getMainPageBookList_Test() throws Exception {
        // Given
        Long userId = 100L;
        MainPageBookListRespDTO mockData = new MainPageBookListRespDTO(null, null);
        when(metadataService.getMainPageBookList(any(Pageable.class), eq(userId))).thenReturn(mockData);

        // When & Then
        mockMvc.perform(get("/api/v2/books/main-page")
                        .header("X-User-Id", userId)
                        .param("page", "0")
                        .param("size", "15"))
                .andExpect(status().isOk());
    }
}