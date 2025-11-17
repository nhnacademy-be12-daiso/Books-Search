package com.daisobook.shop.booksearch.BooksSearch.repository;

import com.daisobook.shop.booksearch.BooksSearch.entity.BookCategory;
import com.daisobook.shop.booksearch.BooksSearch.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findCategoryByName(String name);

    Category findCategoryById(long id);

    List<Category> findAllByBookCategories(List<BookCategory> bookCategories);

    List<Category> findAllByIdIn(List<Long> ids);

    Category findCategoryByNameAndDeep(String name, int deep);
}
