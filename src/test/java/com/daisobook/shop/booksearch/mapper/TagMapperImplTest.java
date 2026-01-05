package com.daisobook.shop.booksearch.mapper;

import com.daisobook.shop.booksearch.dto.response.TagRespDTO;
import com.daisobook.shop.booksearch.entity.tag.BookTag;
import com.daisobook.shop.booksearch.entity.tag.Tag;
import com.daisobook.shop.booksearch.mapper.tag.impl.TagMapperImpl;
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

@ExtendWith(MockitoExtension.class)
class TagMapperImplTest {

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private TagMapperImpl tagMapper;

    @Test
    @DisplayName("BookTag 엔티티 리스트를 TagRespDTO 리스트로 변환한다")
    void toTagRespDTOList_FromEntities_Test() {
        // Given
        Tag tag = new Tag("자바");
        BookTag bookTag = new BookTag();
        bookTag.setTag(tag);
        List<BookTag> bookTags = List.of(bookTag);

        // When
        List<TagRespDTO> result = tagMapper.toTagRespDTOList(bookTags);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().tagId()).isEqualTo(0L);
        assertThat(result.getFirst().tagName()).isEqualTo("자바");
    }

    @Test
    @DisplayName("JSON 문자열 형태의 태그 데이터를 DTO 리스트로 역직렬화한다")
    void toTagRespDTOList_FromJson_Test() throws JsonProcessingException {
        // Given
        String json = "[{\"tagId\":10, \"tagName\":\"스프링\"}, {\"tagId\":11, \"tagName\":\"JPA\"}]";

        // When
        List<TagRespDTO> result = tagMapper.toTagRespDTOList(json);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).tagName()).isEqualTo("스프링");
        assertThat(result.get(1).tagName()).isEqualTo("JPA");
    }

    @Test
    @DisplayName("도서 ID별 태그 JSON 맵을 DTO 리스트 맵으로 정확히 변환한다")
    void toTagRespDTOMap_Test() throws JsonProcessingException {
        // Given
        Long bookId = 100L;
        String json = "[{\"tagId\":1, \"tagName\":\"테스트\"}]";
        Map<Long, String> inputMap = Map.of(bookId, json);

        // When
        Map<Long, List<TagRespDTO>> result = tagMapper.toTagRespDTOMap(inputMap);

        // Then
        assertThat(result).containsKey(bookId);
        assertThat(result.get(bookId).getFirst().tagName()).isEqualTo("테스트");
    }

    @Test
    @DisplayName("null 또는 빈 문자열 입력 시 null을 반환한다")
    void toTagRespDTOList_Empty_Test() throws JsonProcessingException {
        assertThat(tagMapper.toTagRespDTOList((String) null)).isNull();
        assertThat(tagMapper.toTagRespDTOList("")).isNull();
    }
}