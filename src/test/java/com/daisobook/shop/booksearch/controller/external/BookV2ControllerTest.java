package com.daisobook.shop.booksearch.controller.external;

import com.daisobook.shop.booksearch.dto.request.book.BookGroupReqV2DTO;
import com.daisobook.shop.booksearch.dto.request.book.BookReqV2DTO;
import com.daisobook.shop.booksearch.dto.response.book.BookRespDTO;
import com.daisobook.shop.booksearch.service.api.BookRefineService;
import com.daisobook.shop.booksearch.service.book.impl.BookFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookV2Controller.class)
class BookV2ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookFacade bookFacade;

    @MockitoBean
    private BookRefineService bookRefineService;

    @Test
    @DisplayName("신규 도서 등록(Multipart) API 테스트")
    void addBook_Test() throws Exception {
        // Given
        String metadata = "{\"title\":\"테스트 도서\"}";
        MockMultipartFile metadataPart = new MockMultipartFile("metadata", "", "application/json", metadata.getBytes());
        MockMultipartFile image0 = new MockMultipartFile("image0", "test.jpg", "image/jpeg", "data".getBytes());

        BookGroupReqV2DTO groupDto = new BookGroupReqV2DTO(mock(BookReqV2DTO.class), new HashMap<>());
        when(bookFacade.parsing(anyString(), any(), any(), any(), any(), any())).thenReturn(groupDto);
        doNothing().when(bookFacade).registerBook(any(), any());

        // When & Then
        mockMvc.perform(multipart("/api/v2/books")
                        .file(metadataPart)
                        .file(image0)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("도서 수정(PATCH Multipart) API 테스트")
    void modifyBook_Test() throws Exception {
        // Given
        long bookId = 1L;
        String metadata = "{\"title\":\"수정 도서\"}";
        MockMultipartFile metadataPart = new MockMultipartFile("metadata", "", "application/json", metadata.getBytes());

        BookGroupReqV2DTO groupDto = new BookGroupReqV2DTO(mock(BookReqV2DTO.class), new HashMap<>());
        when(bookFacade.parsing(anyString(), any(), any(), any(), any(), any())).thenReturn(groupDto);

        // When & Then (Multipart PATCH 처리를 위해 requestBuilder 커스텀)
        mockMvc.perform(multipart("/api/v2/books/{bookId}", bookId)
                        .file(metadataPart)
                        .with(request -> { request.setMethod("PATCH"); return request; }))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("도서 ID를 통한 삭제 API 테스트")
    void deleteBookById_Test() throws Exception {
        doNothing().when(bookFacade).deleteBookById(1L);

        mockMvc.perform(delete("/api/v2/books/{bookId}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("도서 상세 조회 API 테스트 (Header 포함)")
    void getBookDetail_Test() throws Exception {
        // Given
        long bookId = 1L;
        when(bookFacade.getBookDetail(eq(bookId), any())).thenReturn(mock(BookRespDTO.class));

        // When & Then
        mockMvc.perform(get("/api/v2/books/{bookId}", bookId)
                        .header("X-User-Id", 123L))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ISBN 중복 체크 API 테스트")
    void getBookRegisterInfoByIsbn_Test() throws Exception {
        String isbn = "1234567890";
        when(bookFacade.existIsbn(isbn)).thenReturn(true);

        mockMvc.perform(get("/api/v2/books/isbn/{isbn}/exist", isbn))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("카테고리별 도서 목록 조회 API 테스트")
    void getBookListByCategoryId_Test() throws Exception {
        mockMvc.perform(get("/api/v2/books/categories/{categoryId}", 1L)
                        .param("page", "0")
                        .param("size", "15"))
                .andExpect(status().isOk());
    }
}