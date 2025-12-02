package com.daisobook.shop.booksearch.BooksSearch.repository;

import com.daisobook.shop.booksearch.BooksSearch.entity.BookCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookCategoryRepository extends JpaRepository<BookCategory, Long> {
    List<BookCategory> findAllByBook_Id(long bookId);

    void deleteBookCategoriesByIdIn(List<Long> ids);

    boolean existsBookCategoriesByCategory_Id(long categoryId);
}
