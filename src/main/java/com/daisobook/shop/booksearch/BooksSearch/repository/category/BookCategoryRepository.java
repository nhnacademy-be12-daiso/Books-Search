package com.daisobook.shop.booksearch.BooksSearch.repository.category;

import com.daisobook.shop.booksearch.BooksSearch.entity.category.BookCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookCategoryRepository extends JpaRepository<BookCategory, Long> {
    Optional<BookCategory> findByBook_Id(long bookId);

    List<BookCategory> findAllByBook_Id(long bookId);

    void deleteBookCategoriesByIdIn(List<Long> ids);

    boolean existsBookCategoriesByCategory_Id(long categoryId);

    void removeAllByIdIn(List<Long> ids);
}
