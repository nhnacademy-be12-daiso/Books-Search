package com.daisobook.shop.booksearch.BooksSearch.mapper;

import com.daisobook.shop.booksearch.BooksSearch.dto.projection.CategoryListProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.category.CategoryList;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.category.CategoryRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.category.CategoryTree;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.category.NotFoundCategoryId;
import com.daisobook.shop.booksearch.BooksSearch.mapper.category.impl.CategoryMapperImpl;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryMapperImplTest {

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private CategoryMapperImpl categoryMapper;

    @Test
    @DisplayName("CategoryListProjection 목록을 받아 부모 경로를 포함한 문자열 경로 리스트를 생성한다")
    void toCategoryListTest() {
        // Given
        CategoryListProjection parent = mock(CategoryListProjection.class);
        when(parent.getCategoryId()).thenReturn(1L);
        when(parent.getCategoryName()).thenReturn("국내도서");
        when(parent.getPreCategoryId()).thenReturn(null);

        CategoryListProjection child = mock(CategoryListProjection.class);
        when(child.getCategoryId()).thenReturn(2L);
        when(child.getCategoryName()).thenReturn("소설");
        when(child.getPreCategoryId()).thenReturn(1L);

        Map<Long, CategoryListProjection> projectionMap = Map.of(1L, parent, 2L, child);
        List<CategoryListProjection> leafList = List.of(child);

        // When
        CategoryList result = categoryMapper.toCategoryList(projectionMap, leafList);

        // Then
        assertThat(result.categoryPathList()).hasSize(1);
        // 기대 경로 포맷: "> 1:국내도서 > 2:소설"
        assertThat(result.categoryPathList().getFirst().path())
                .contains("1:국내도서")
                .contains("2:소설");
    }

    @Test
    @DisplayName("평면 구조의 카테고리 리스트를 계층형 트리 구조로 변환한다")
    void toCategoryTreeListTest() {
        // Given
        CategoryListProjection p1 = createMockProjection(1L, "ROOT", null);
        CategoryListProjection p2 = createMockProjection(2L, "SUB", 1L);
        List<CategoryListProjection> projections = List.of(p1, p2);

        // When
        List<CategoryTree> result = categoryMapper.toCategoryTreeList(projections);

        // Then
        assertThat(result).hasSize(1); // 최상위는 하나
        assertThat(result.get(0).categoryId()).isEqualTo(1L);
        assertThat(result.get(0).children()).hasSize(1);
        assertThat(result.get(0).children().get(0).categoryId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("트리 생성 중 상위 카테고리 ID가 Map에 없으면 NotFoundCategoryId 예외가 발생한다")
    void toCategoryTreeList_Exception_Test() {
        // Given: 부모 ID는 1인데, 1번 데이터는 리스트에 없는 경우
        CategoryListProjection p2 = createMockProjection(2L, "SUB", 1L);
        List<CategoryListProjection> projections = List.of(p2);

        // When & Then
        assertThatThrownBy(() -> categoryMapper.toCategoryTreeList(projections))
                .isInstanceOf(NotFoundCategoryId.class)
                .hasMessageContaining("관계 불일치");
    }

    @Test
    @DisplayName("JSON 문자열을 CategoryRespDTO 리스트로 역직렬화한다")
    void toCategoryRespDTOList_Json_Test() throws Exception {
        // Given
        String json = "[{\"categoryId\":1,\"categoryName\":\"테스트\",\"deep\":1}]";

        // When
        List<CategoryRespDTO> result = categoryMapper.toCategoryRespDTOList(json);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().categoryName()).isEqualTo("테스트");
    }

    // 편의를 위한 Mock 생성 메서드
    private CategoryListProjection createMockProjection(Long id, String name, Long preId) {
        CategoryListProjection p = mock(CategoryListProjection.class);
        when(p.getCategoryId()).thenReturn(id);
        when(p.getCategoryName()).thenReturn(name);
        when(p.getPreCategoryId()).thenReturn(preId);
        return p;
    }
}