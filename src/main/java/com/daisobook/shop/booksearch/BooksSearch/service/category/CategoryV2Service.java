package com.daisobook.shop.booksearch.BooksSearch.service.category;

import com.daisobook.shop.booksearch.BooksSearch.dto.coupon.response.BookCategoryResponse;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.category.CategoryModifyReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.category.CategoryRegisterReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.category.CategoryList;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.category.CategoryTreeListRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.coupon.CategorySimpleResponse;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;

import java.util.List;
import java.util.Map;

public interface CategoryV2Service {
    void registerCategory(CategoryRegisterReqDTO reqDTO);
    void modifyCategory(long categoryId, CategoryModifyReqDTO reqDTO);
    void deleteCategory(long categoryId);
    //bookCoreService에서 사용하는 메서드
    void assignCategoriesToBook(Book book, Long categoryId);
    void assignCategoriesToBooks(Map<String, Book> bookMap, Map<String, Long> categoryIdMap);
    void updateCategoryOfBook(Book book, Long categoryId);
    void deleteCategoryOfBook(Book book);
    CategoryList getCategoryList();
    Long getCountAll();
    CategoryTreeListRespDTO getCategoryTreeList();
    BookCategoryResponse bookCategory(Long bookId);
    List<Long> getLowCategoryIdList(long categoryId);
    //쿠폰 용
    List<CategorySimpleResponse> findByIdIn(List<Long> categoryIds);
}
