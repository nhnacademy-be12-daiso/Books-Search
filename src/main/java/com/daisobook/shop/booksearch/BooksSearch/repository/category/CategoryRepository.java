package com.daisobook.shop.booksearch.BooksSearch.repository.category;

import com.daisobook.shop.booksearch.BooksSearch.entity.category.BookCategory;
import com.daisobook.shop.booksearch.BooksSearch.entity.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;

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

}
