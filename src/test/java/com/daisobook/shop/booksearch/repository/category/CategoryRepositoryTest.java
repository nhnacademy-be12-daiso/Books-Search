package com.daisobook.shop.booksearch.repository.category;

import com.daisobook.shop.booksearch.dto.projection.CategoryListProjection;
import com.daisobook.shop.booksearch.dto.projection.CategoryPathProjection;
import com.daisobook.shop.booksearch.entity.category.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Category rootCategory;
    private Category midCategory;
    private Category leafCategory;

    @BeforeEach
    void setUp() {
        // 계층 구조 생성: 도서 -> 국내도서 -> 소설
        
        // 1. 최상위 카테고리 (Deep 1)
        rootCategory = new Category(1, "도서", 1);
        entityManager.persist(rootCategory);

        // 2. 중간 카테고리 (Deep 2)
        midCategory = new Category(11,"국내도서", 2);
        midCategory.setPreCategory(rootCategory); // 부모 설정
        entityManager.persist(midCategory);

        // 3. 최하위 카테고리 (Deep 3)
        leafCategory = new Category(111,"소설", 3);
        leafCategory.setPreCategory(midCategory); // 부모 설정
        entityManager.persist(leafCategory);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("이름으로 카테고리를 조회한다")
     void findCategoryByNameTest() {
        Category result = categoryRepository.findCategoryByName("소설");
        assertThat(result).isNotNull();
        assertThat(result.getDeep()).isEqualTo(3);
    }

    @Test
    @DisplayName("카테고리 ID 존재 여부를 확인한다")
    void existsCategoryByIdTest() {
        boolean exists = categoryRepository.existsCategoryById(leafCategory.getId());
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("최하위 카테고리 ID로 상위 카테고리 경로를 모두 조회한다 (Recursive)")
    void findAncestorsPathByFinalCategoryIdTest() {
        /*
           주의: WITH RECURSIVE는 H2 DB MySQL 모드에서 지원되나, 
           버전에 따라 문법 차이가 있을 수 있습니다.
        */
        List<CategoryPathProjection> path = categoryRepository.findAncestorsPathByFinalCategoryId(leafCategory.getId());

        // 소설(3) -> 국내도서(2) -> 도서(1) 총 3개가 조회되어야 함
        assertThat(path).hasSize(3);
    }

    @Test
    @DisplayName("모든 카테고리 목록을 Projection 형태로 가져온다")
    void getAllTest() {
        List<CategoryListProjection> allCategories = categoryRepository.getAll();
        assertThat(allCategories).hasSize(3);
    }

    @Test
    @DisplayName("특정 카테고리를 부모로 가진 하위 카테고리가 존재하는지 확인한다")
    void existsCategoriesByPreCategory_IdTest() {
        // root(도서)는 mid(국내도서)를 자식으로 가지고 있음
        boolean hasChild = categoryRepository.existsCategoriesByPreCategory_Id(rootCategory.getId());
        assertThat(hasChild).isTrue();
    }

    @Test
    @DisplayName("상위 카테고리 ID로 모든 하위 카테고리 ID 리스트를 가져온다 (Recursive)")
    void getLowCategoryIdTest() {
        // root(도서) ID를 넣으면 하위인 mid, leaf를 포함한 ID 리스트 반환
        List<Long> childIds = categoryRepository.getLowCategoryId(rootCategory.getId());

        assertThat(childIds).contains(rootCategory.getId(), midCategory.getId(), leafCategory.getId());
    }
}