package com.daisobook.shop.booksearch.books_search.mapper;

import com.daisobook.shop.booksearch.books_search.dto.projection.CategoryListProjection;
import com.daisobook.shop.booksearch.books_search.dto.response.category.CategoryList;
import com.daisobook.shop.booksearch.books_search.dto.response.category.CategoryRespDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.category.CategoryTree;
import com.daisobook.shop.booksearch.books_search.entity.category.BookCategory;
import com.daisobook.shop.booksearch.books_search.entity.category.Category;
import com.daisobook.shop.booksearch.books_search.exception.custom.category.NotFoundCategoryId;
import com.daisobook.shop.booksearch.books_search.mapper.category.impl.CategoryMapperImpl;
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

    // --- 1. Entity to DTO Mapping ---

    @Test
    @DisplayName("toCategoryRespDTOList(Entity): BookCategory 엔티티 리스트를 DTO 리스트로 변환한다")
    void toCategoryRespDTOList_Entity_Test() {
        // Given
        Category pre = new Category(1L, "대분류", 1);
        Category child = new Category(2L, "소분류", 2);
        child.setPreCategory(pre);

        BookCategory bc = mock(BookCategory.class);
        when(bc.getCategory()).thenReturn(child);

        // When
        List<CategoryRespDTO> result = categoryMapper.toCategoryRespDTOList(List.of(bc));

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().categoryName()).isEqualTo("소분류");
        assertThat(result.getFirst().preCategoryId()).isEqualTo(1L);
        assertThat(result.getFirst().preCategoryName()).isEqualTo("대분류");
    }

    // --- 2. JSON String / Map Mapping ---

    @Test
    @DisplayName("toCategoryRespDTOList(String): 빈 문자열이나 null 입력 시 null을 반환한다")
    void toCategoryRespDTOList_Json_EdgeCase_Test() throws Exception {
        assertThat(categoryMapper.toCategoryRespDTOList((String) null)).isNull();
        assertThat(categoryMapper.toCategoryRespDTOList("  ")).isNull();
    }

    @Test
    @DisplayName("toCategoryRespDTOMap: JSON 문자열 Map을 DTO 리스트 Map으로 변환한다")
    void toCategoryRespDTOMap_Test() throws Exception {
        // Given
        String json = "[{\"categoryId\":10,\"categoryName\":\"IT\"}]";
        Map<Long, String> inputMap = Map.of(100L, json);

        // When
        Map<Long, List<CategoryRespDTO>> result = categoryMapper.toCategoryRespDTOMap(inputMap);

        // Then
        assertThat(result).containsKey(100L);
        assertThat(result.get(100L).getFirst().categoryName()).isEqualTo("IT");
    }

    // --- 3. Path & Tree Logic (정밀 검증) ---

    @Test
    @DisplayName("toCategoryList: 다단계(3단계) 계층 구조에서 정확한 경로 문자열을 생성하는지 확인")
    void toCategoryList_DeepPath_Test() {
        // Given: 1(대) > 2(중) > 3(소)
        CategoryListProjection p1 = createMockProjection(1L, "국내", null);
        CategoryListProjection p2 = createMockProjection(2L, "소설", 1L);
        CategoryListProjection p3 = createMockProjection(3L, "판타지", 2L);

        Map<Long, CategoryListProjection> projectionMap = Map.of(1L, p1, 2L, p2, 3L, p3);
        List<CategoryListProjection> leafList = List.of(p3);

        // When
        CategoryList result = categoryMapper.toCategoryList(projectionMap, leafList);

        // Then
        // 기대값 포맷: "> 1:국내 > 2:소설 > 3:판타지"
        String expectedPath = "> 1:국내 > 2:소설 > 3:판타지";
        assertThat(result.categoryPathList().getFirst().path()).isEqualTo(expectedPath);
    }

    @Test
    @DisplayName("toCategoryTreeList: 상위 카테고리가 없는(ROOT) 항목들이 트리 리스트로 모이는지 확인")
    void toCategoryTreeList_MultipleRoots_Test() {
        // Given: 루트가 2개인 경우
        CategoryListProjection r1 = createMockProjection(1L, "ROOT1", null);
        CategoryListProjection r2 = createMockProjection(2L, "ROOT2", null);
        CategoryListProjection c1 = createMockProjection(3L, "CHILD1", 1L);

        // When
        List<CategoryTree> result = categoryMapper.toCategoryTreeList(List.of(r1, r2, c1));

        // Then
        assertThat(result).hasSize(2); // ROOT1, ROOT2
        CategoryTree root1 = result.stream().filter(t -> t.categoryId() == 1L).findFirst().get();
        assertThat(root1.children()).hasSize(1);
        assertThat(root1.children().getFirst().categoryId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("toCategoryTreeList: 상위 카테고리 ID 불일치 시 예외 메시지에 상세 정보가 포함되는지 확인")
    void toCategoryTreeList_Exception_Detail_Test() {
        // Given: 부모 999는 존재하지 않음
        CategoryListProjection child = createMockProjection(10L, "에러노드", 999L);
        List<CategoryListProjection> projections = List.of(child);

        // When & Then
        assertThatThrownBy(() -> categoryMapper.toCategoryTreeList(projections))
                .isInstanceOf(NotFoundCategoryId.class)
                .hasMessageContaining("관계 불일치");
    }

    // --- Helper ---
    private CategoryListProjection createMockProjection(Long id, String name, Long preId) {
        CategoryListProjection p = mock(CategoryListProjection.class);
        when(p.getCategoryId()).thenReturn(id);
        when(p.getCategoryName()).thenReturn(name);
        when(p.getPreCategoryId()).thenReturn(preId);
        return p;
    }
}