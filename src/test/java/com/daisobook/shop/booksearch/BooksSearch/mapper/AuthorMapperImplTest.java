package com.daisobook.shop.booksearch.BooksSearch.mapper;

import com.daisobook.shop.booksearch.BooksSearch.dto.response.AuthorRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.author.Author;
import com.daisobook.shop.booksearch.BooksSearch.entity.author.BookAuthor;
import com.daisobook.shop.booksearch.BooksSearch.entity.author.Role;
import com.daisobook.shop.booksearch.BooksSearch.mapper.author.impl.AuthorMapperImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
class AuthorMapperImplTest {

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AuthorMapperImpl authorMapper;

    private Author author;
    private Role role;

    @BeforeEach
    void setUp() {
        author = new Author("김작가");
        role = new Role("지은이");
    }

    @Test
    @DisplayName("BookAuthor 엔티티 리스트를 AuthorRespDTO 리스트로 정확히 변환한다")
    void toAuthorRespDTOList_FromEntities_Test() {
        // Given
        BookAuthor bookAuthor = new BookAuthor();
        bookAuthor.setAuthor(author);
        bookAuthor.setRole(role);
        List<BookAuthor> bookAuthors = List.of(bookAuthor);

        // When
        List<AuthorRespDTO> result = authorMapper.toAuthorRespDTOList(bookAuthors);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().authorId()).isEqualTo(0L);
        assertThat(result.getFirst().authorName()).isEqualTo("김작가");
        assertThat(result.getFirst().roleName()).isEqualTo("지은이");
    }

    @Test
    @DisplayName("JSON 문자열을 AuthorRespDTO 리스트로 역직렬화한다")
    void toAuthorRespDTOList_FromJsonString_Test() throws JsonProcessingException {
        // Given
        String json = "[{\"authorId\":1,\"authorName\":\"김작가\",\"roleId\":1,\"roleName\":\"지은이\"}]";

        // When
        List<AuthorRespDTO> result = authorMapper.toAuthorRespDTOList(json);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().authorName()).isEqualTo("김작가");
    }

    @Test
    @DisplayName("빈 문자열이나 null 입력 시 null을 반환한다")
    void toAuthorRespDTOList_EmptyInput_Test() throws JsonProcessingException {
        assertThat(authorMapper.toAuthorRespDTOList((List<BookAuthor>) null)).isNull();
        assertThat(authorMapper.toAuthorRespDTOList("  ")).isNull();
    }

    @Test
    @DisplayName("Map 형태의 JSON 데이터를 DTO 맵으로 정확히 변환한다")
    void toAuthorRespDTOMap_Test() throws JsonProcessingException {
        // Given
        String json = "[{\"authorId\":1,\"authorName\":\"김작가\"}]";
        Map<Long, String> inputMap = Map.of(100L, json);

        // When
        Map<Long, List<AuthorRespDTO>> result = authorMapper.toAuthorRespDTOMap(inputMap);

        // Then
        assertThat(result).containsKey(100L);
        assertThat(result.get(100L).getFirst().authorName()).isEqualTo("김작가");
    }
}