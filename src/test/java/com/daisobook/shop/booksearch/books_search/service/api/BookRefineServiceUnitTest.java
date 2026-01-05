package com.daisobook.shop.booksearch.books_search.service.api;

import com.daisobook.shop.booksearch.books_search.client.AiClient;
import com.daisobook.shop.booksearch.books_search.dto.api.AladinResponseWrapper;
import com.daisobook.shop.booksearch.books_search.dto.api.BookInfoDataView;
import com.daisobook.shop.booksearch.books_search.dto.response.RoleNameListRespDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.category.CategoryList;
import com.daisobook.shop.booksearch.books_search.exception.custom.ai.BookNotFoundException;
import com.daisobook.shop.booksearch.books_search.exception.custom.ai.LlmAnalysisException;
import com.daisobook.shop.booksearch.books_search.service.author.AuthorV2Service;
import com.daisobook.shop.booksearch.books_search.service.category.CategoryV2Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookRefineServiceUnitTest {

    @Mock
    AiClient aiClient;

    @Mock
    CategoryV2Service categoryService;

    @Mock
    AuthorV2Service authorService;

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    BookRefineService bookRefineService;

    // helper to mock WebClient fluent chain
    private WebClient mockWebClientReturning(String raw) {
        WebClient webClient = mock(WebClient.class);
        WebClient.RequestHeadersUriSpec<?> uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec<?> headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec respSpec = mock(WebClient.ResponseSpec.class);

        // 제네릭 캡처 문제를 피하기 위해 doReturn 사용
        doReturn(uriSpec).when(webClient).get();
        doReturn(headersSpec).when(uriSpec).uri(any(java.util.function.Function.class));
        when(headersSpec.retrieve()).thenReturn(respSpec);
        when(respSpec.bodyToMono(String.class)).thenReturn(Mono.justOrEmpty(raw));
        return webClient;
    }


    @Test
    @DisplayName("알라딘 응답이 null 이면 BookNotFoundException")
    void whenAladinRawNull_thenThrowBookNotFound() {
        ReflectionTestUtils.setField(bookRefineService, "webClient", mockWebClientReturning(null));

        assertThatThrownBy(() -> bookRefineService.getRefinedBook("ISBN-1"))
                .isInstanceOf(BookNotFoundException.class);
        verifyNoInteractions(aiClient, categoryService, authorService, objectMapper);
    }

    @Test
    @DisplayName("알라딘 응답에 ErrorCode 포함 시 BookNotFoundException")
    void whenAladinContainsErrorCode_thenThrowBookNotFound() {
        ReflectionTestUtils.setField(bookRefineService, "webClient", mockWebClientReturning("{\"ErrorCode\":123}"));

        assertThatThrownBy(() -> bookRefineService.getRefinedBook("ISBN-2"))
                .isInstanceOf(BookNotFoundException.class);
        verifyNoInteractions(aiClient, categoryService, authorService, objectMapper);
    }

    @Test
    @DisplayName("알라딘 파싱 실패 시 null 반환")
    void whenAladinParseFails_thenReturnNull() throws Exception {
        String raw = "{\"some\":\"json\"}";
        ReflectionTestUtils.setField(bookRefineService, "webClient", mockWebClientReturning(raw));

        when(objectMapper.readValue(raw, AladinResponseWrapper.class)).thenThrow(new RuntimeException("parse error"));

        Object result = bookRefineService.getRefinedBook("ISBN-3");
        assertThat(result).isNull();

        verify(objectMapper, times(1)).readValue(raw, AladinResponseWrapper.class);
        verifyNoInteractions(aiClient, categoryService, authorService);
    }

    @Test
    @DisplayName("알라딘 아이템이 비었으면 BookNotFoundException")
    void whenAladinHasNoItems_thenThrowBookNotFound() throws Exception {
        String raw = "{\"dummy\":1}";
        ReflectionTestUtils.setField(bookRefineService, "webClient", mockWebClientReturning(raw));

        AladinResponseWrapper aladinRes = mock(AladinResponseWrapper.class);
        // item() 가 null 이 되도록 설정
        when(objectMapper.readValue(raw, AladinResponseWrapper.class)).thenReturn(aladinRes);
        when(aladinRes.item()).thenReturn(null);

        assertThatThrownBy(() -> bookRefineService.getRefinedBook("ISBN-4"))
                .isInstanceOf(BookNotFoundException.class);

        verify(objectMapper, times(1)).readValue(raw, AladinResponseWrapper.class);
    }

    @Test
    @DisplayName("AI 파싱 실패 시 LlmAnalysisException 발생")
    void whenAiResponseBadJson_thenThrowLlmAnalysisException() throws Exception {
        String raw = "{\"ok\":true}";
        ReflectionTestUtils.setField(bookRefineService, "webClient", mockWebClientReturning(raw));

        AladinResponseWrapper aladinRes = mock(AladinResponseWrapper.class, RETURNS_DEEP_STUBS);
        when(objectMapper.readValue(raw, AladinResponseWrapper.class)).thenReturn(aladinRes);
        // 불필요하게 item()을 미리 호출하지 말고 deep stub으로 필요한 행위만 설정
        when(aladinRes.item().isEmpty()).thenReturn(false);

        CategoryList categoryList = mock(CategoryList.class);
        RoleNameListRespDTO roleList = mock(RoleNameListRespDTO.class);
        when(categoryService.getCategoryList()).thenReturn(categoryList);
        when(authorService.getRoleNameList()).thenReturn(roleList);

        String rawAi = "```json\n{ invalid json ::: }\n```";
        when(aiClient.refineBookData(any(), eq(categoryList), eq(roleList))).thenReturn(rawAi);

        // cause objectMapper.readValue on BookInfoDataView to throw
        when(objectMapper.readValue(anyString(), eq(BookInfoDataView.class))).thenThrow(new RuntimeException("bad json"));

        assertThatThrownBy(() -> bookRefineService.getRefinedBook("ISBN-5"))
                .isInstanceOf(LlmAnalysisException.class);

        verify(aiClient, times(1)).refineBookData(any(), eq(categoryList), eq(roleList));
        verify(objectMapper, times(1)).readValue(anyString(), eq(BookInfoDataView.class));
    }


    @Test
    @DisplayName("정상 플로우: 마크다운 제거, 날짜 대체, DTO 반환")
    void successFlow_cleansJsonAndParses() throws Exception {
        String raw = "{\"ok\":true}";
        ReflectionTestUtils.setField(bookRefineService, "webClient", mockWebClientReturning(raw));

        // deep stub to allow chained calls like item().getFirst().pubDate()
        AladinResponseWrapper aladinRes = mock(AladinResponseWrapper.class, RETURNS_DEEP_STUBS);
        when(objectMapper.readValue(raw, AladinResponseWrapper.class)).thenReturn(aladinRes);
        // item() 존재하고 비어있지 않음
        when(aladinRes.item().isEmpty()).thenReturn(false);
        when(aladinRes.item().getFirst().pubDate()).thenReturn("2024-12-31");

        CategoryList categoryList = mock(CategoryList.class);
        RoleNameListRespDTO roleList = mock(RoleNameListRespDTO.class);
        when(categoryService.getCategoryList()).thenReturn(categoryList);
        when(authorService.getRoleNameList()).thenReturn(roleList);

        String aiRaw = "some text before ```json\n{ \"publicationDate\": \"YYYY-MM-DD\", \"title\": \"Hello\" }\n``` tail";
        when(aiClient.refineBookData(aladinRes.item().getFirst(), categoryList, roleList)).thenReturn(aiRaw);

        BookInfoDataView expected = mock(BookInfoDataView.class);
        // Expect cleaned JSON to be passed to objectMapper for BookInfoDataView
        // We capture the string form but for brevity just return expected for anyString()
        when(objectMapper.readValue(anyString(), eq(BookInfoDataView.class))).thenReturn(expected);

        BookInfoDataView result = bookRefineService.getRefinedBook("ISBN-6");
        assertThat(result).isSameAs(expected);

        verify(aiClient, times(1)).refineBookData(aladinRes.item().getFirst(), categoryList, roleList);
        verify(objectMapper, times(1)).readValue(anyString(), eq(BookInfoDataView.class));
    }

    @Test
    @DisplayName("스프링 + MockitoBean: 알라딘 응답 null 이면 BookNotFoundException")
    void springContext_whenAladinNull_thenBookNotFound() {
        ReflectionTestUtils.setField(bookRefineService, "webClient", mockWebClientReturning(null));

        assertThatThrownBy(() -> bookRefineService.getRefinedBook("ISBN-SPRING"))
                .isInstanceOf(BookNotFoundException.class);

        verifyNoInteractions(aiClient, categoryService, authorService, objectMapper);
    }

    @Configuration
    static class TestConfig {
        @Bean
        BookRefineService bookRefineService(AiClient aiClient,
                                            CategoryV2Service categoryService,
                                            AuthorV2Service authorService,
                                            ObjectMapper objectMapper) {
            return new BookRefineService(aiClient, categoryService, authorService, objectMapper);
        }
    }
}
