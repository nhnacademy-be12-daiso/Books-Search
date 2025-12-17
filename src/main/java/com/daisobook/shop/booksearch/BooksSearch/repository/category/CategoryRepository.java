package com.daisobook.shop.booksearch.BooksSearch.repository.category;

import com.daisobook.shop.booksearch.BooksSearch.dto.projection.CategoryPathProjection;
import com.daisobook.shop.booksearch.BooksSearch.entity.category.BookCategory;
import com.daisobook.shop.booksearch.BooksSearch.entity.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findCategoryByName(String name);

    Category findCategoryById(long id);

    List<Category> findAllByBookCategories(List<BookCategory> bookCategories);

    List<Category> findAllByIdIn(List<Long> ids);

    Category findCategoryByNameAndDeep(String name, int deep);

    List<Category> findAllByNameInAndDeepIn(Collection<String> names, List<Integer> deeps);

    boolean existsCategoryByName(String name);

    List<Category> findAllByNameIn(Collection<String> names);

    boolean existsCategoryById(long id);

    List<Category> findAllByPreCategory_Id(long preCategoryId);

    List<Category> findAllByPreCategory_Name(String preCategoryName);

    List<Category> findAllByDeep(int deep);

    List<Category> findByIdIn(List<Long> ids);

    @Query(
            value = """
            WITH RECURSIVE CategoryPath (id, pre_category_id, deep) AS (
                -- 앵커 멤버 (재귀 시작: 최종 카테고리)
                SELECT c.category_id, c.pre_category_id, c.deep
                FROM categories c
                WHERE c.category_id = ?1
                            
                UNION ALL
                            
                -- 재귀 멤버 (부모를 타고 올라감)
                SELECT c.category_id, c.pre_category_id, c.deep
                FROM categories c
                JOIN CategoryPath cp ON c.category_id = cp.pre_category_id -- 💡 부모 ID를 타고 위로
            )
            -- 💡 DTO에 맞게 컬럼명 선택
            SELECT id, pre_category_id, deep FROM CategoryPath
            """,
            nativeQuery = true
    )
    List<CategoryPathProjection> findAncestorsPathByFinalCategoryId(Long finalCategoryId);

    @Query(
            value = """
            WITH RECURSIVE CategoryPath (id, pre_category_id, deep) AS (
                -- 앵커 멤버 (재귀 시작: 최종 카테고리)
                SELECT c.category_id, c.pre_category_id, c.deep
                FROM categories c
                WHERE c.category_id IN :?1 -- 리스트 형태로 IN (...) 들어가기 위해서는 :?1 #?1 둘중에 하나를 사용 - nativeQuery에서만 작동
                            
                UNION ALL
                            
                -- 재귀 멤버 (부모를 타고 올라감)
                SELECT c.category_id, c.pre_category_id, c.deep
                FROM categories c
                JOIN CategoryPath cp ON c.category_id = cp.pre_category_id -- 💡 부모 ID를 타고 위로
            )
            -- 💡 DTO에 맞게 컬럼명 선택
            SELECT DISTINCT id, pre_category_id, deep FROM CategoryPath
            """,
            nativeQuery = true
    )
    List<CategoryPathProjection> findAncestorsPathByFinalCategoryIdIn(List<Long> finalCategoryIds);

}
