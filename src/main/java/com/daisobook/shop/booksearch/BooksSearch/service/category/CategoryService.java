package com.daisobook.shop.booksearch.BooksSearch.service.category;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.CategoryReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.CategoryRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.BookCategory;
import com.daisobook.shop.booksearch.BooksSearch.entity.Category;

import java.util.List;

public interface CategoryService {
    void validateNotExistsByName(String categoryName);
    void validateExistsById(long categoryId);
    void validateExistsByName(String categoryName);
    void registerCategory(CategoryReqDTO categoryReqDTO);
    void registerCategories(List<CategoryReqDTO> categoryReqDTOList);
    CategoryRespDTO getCategoryById(long categoryId);
    CategoryRespDTO getCategoryByName(String categoryName);
    List<CategoryRespDTO> getTopCategories();
    List<CategoryRespDTO> getCategoriesByDeep(int deep);
    //해당 카테고리 아이디의 하위 카테고리 가져오기
    List<CategoryRespDTO> getSubCategories(long categoryId);
    List<CategoryRespDTO> getSubCategories(String categoryName);
    void updateCategory(long categoryId, CategoryReqDTO categoryReqDTO);
    void deleteCategory(long categoryId);

    //bookService에서 사용되는 메서드
    Category findValidCategoryByNameAndDeep(String categoryName, int deep);
    List<Category> findCategoriesByIds(List<Long> categoryIds);
    List<CategoryRespDTO> getCategoryDTOsByIds(List<Long> categoryIds);
    List<Category> findCategoriesByNamesAndDeeps(List<String> categoryNames, List<Integer> deeps);
    List<Category> findAllByBookCategories(List<BookCategory> bookCategories);
}
