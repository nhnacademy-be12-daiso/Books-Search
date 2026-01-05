package com.daisobook.shop.booksearch.books_search.mapper;

import com.daisobook.shop.booksearch.books_search.dto.response.PublisherRespDTO;
import com.daisobook.shop.booksearch.books_search.mapper.publisher.impl.PublisherMapperImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PublisherMapperImplTest {

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private PublisherMapperImpl publisherMapper;

    @Test
    @DisplayName("출판사 정보 맵(JSON 문자열)을 DTO 맵으로 변환한다")
    void toPublisherRespDTOMap_Test() throws JsonProcessingException {
        // Given
        Long bookId = 1L;
        String publisherJson = "{\"id\":10, \"name\":\"다이소출판사\"}";
        Map<Long, String> inputMap = Map.of(bookId, publisherJson);

        // When
        Map<Long, PublisherRespDTO> result = publisherMapper.toPublisherRespDTOMap(inputMap);

        // Then
        assertThat(result).containsKey(bookId);
        assertThat(result.get(bookId).name()).isEqualTo("다이소출판사");
        assertThat(result.get(bookId).id()).isEqualTo(10L);
    }

    @Test
    @DisplayName("단일 출판사 JSON 문자열을 DTO로 변환한다")
    void toPublisherRespDTO_Test() throws JsonProcessingException {
        // Given
        String publisherJson = "{\"id\":20, \"name\":\"코드마스터\"}";

        // When
        PublisherRespDTO result = publisherMapper.toPublisherRespDTO(publisherJson);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("코드마스터");
    }

    @Test
    @DisplayName("입력 문자열이 비어있거나 null이면 null을 반환한다")
    void toPublisherRespDTO_Empty_Test() throws JsonProcessingException {
        // When & Then
        assertThat(publisherMapper.toPublisherRespDTO(null)).isNull();
        assertThat(publisherMapper.toPublisherRespDTO("   ")).isNull();
    }
}