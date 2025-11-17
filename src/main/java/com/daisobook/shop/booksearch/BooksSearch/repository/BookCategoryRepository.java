package com.daisobook.shop.booksearch.BooksSearch.repository;

import com.daisobook.shop.booksearch.BooksSearch.entity.BookCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookCategoryRepository extends JpaRepository<BookCategory, Long> {
    boolean existsBookCategoryByBookId(long bookId);

    BookCategory findAllById(long id);

    List<BookCategory> findAllByBook_Id(long bookId);

    void deleteBookCategoriesByIdIn(List<Long> ids);
}
