package com.daisobook.shop.booksearch.BooksSearch.mapper;

import com.daisobook.shop.booksearch.BooksSearch.dto.DiscountDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.LikeBookListProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.AuthorRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.like.LikeListRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.mapper.author.AuthorMapper;
import com.daisobook.shop.booksearch.BooksSearch.mapper.like.impl.LikeMapperImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LikeMapperImplTest {

    @Mock
    private AuthorMapper authorMapper;

    @InjectMocks
    private LikeMapperImpl likeMapper;

    @Test
    @DisplayName("LikeBookListProjection 리스트와 할인 정보를 조합하여 LikeListRespDTO 리스트를 생성한다")
    void toLikeListRespDTOList_Test() throws JsonProcessingException {
        // Given
        Long bookId = 1L;
        String createdAt = "2024-01-01T10:00:00+09:00[Asia/Seoul]";
        
        LikeBookListProjection lp = mock(LikeBookListProjection.class);
        when(lp.getBookId()).thenReturn(bookId);
        when(lp.getAuthors()).thenReturn("authors-json");
        when(lp.getCreatedAt()).thenReturn(createdAt);
        when(lp.getPrice()).thenReturn(20000L);
        when(lp.getTitle()).thenReturn("찜한 도서");

        // 하위 매퍼 Mock 설정 (저자 정보)
        List<AuthorRespDTO> authorDtos = List.of(new AuthorRespDTO(1L, "작가", 1L, "저자"));
        when(authorMapper.toAuthorRespDTOMap(anyMap())).thenReturn(Map.of(bookId, authorDtos));

        // 할인 정보 Map 설정 (10% 할인 가정)
        DiscountDTO.Response discount = new DiscountDTO.Response(1L, 18000L, new BigDecimal("10.00"), 2000L);
        Map<Long, DiscountDTO.Response> discountMap = Map.of(bookId, discount);

        // When
        List<LikeListRespDTO> result = likeMapper.toLikeListRespDTOList(List.of(lp), discountMap);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().bookId()).isEqualTo(bookId);
        assertThat(result.getFirst().discountPercentage()).isEqualTo(new BigDecimal("10.00"));
        assertThat(result.getFirst().authorRespDTOList()).hasSize(1);
        assertThat(result.getFirst().createdAt()).isEqualTo(ZonedDateTime.parse(createdAt));
    }

    @Test
    @DisplayName("Projection 리스트를 할인 요청용 DTO 맵으로 변환한다")
    void toDiscountDTOMap_Test() {
        // Given
        LikeBookListProjection lp = mock(LikeBookListProjection.class);
        when(lp.getBookId()).thenReturn(10L);
        when(lp.getPrice()).thenReturn(15000L);

        // When
        Map<Long, DiscountDTO.Request> result = likeMapper.toDiscountDTOMap(List.of(lp));

        // Then
        assertThat(result).containsKey(10L);
        assertThat(result.get(10L).price()).isEqualTo(15000L);
    }
}