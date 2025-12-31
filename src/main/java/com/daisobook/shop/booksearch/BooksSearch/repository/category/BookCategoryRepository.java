package com.daisobook.shop.booksearch.BooksSearch.repository.category;

import com.daisobook.shop.booksearch.BooksSearch.entity.category.BookCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookCategoryRepository extends JpaRepository<BookCategory, Long> {
    List<BookCategory> findAllByBook_Id(long bookId);

    void deleteBookCategoriesByIdIn(List<Long> ids);

    boolean existsBookCategoriesByCategory_Id(long categoryId);

    void removeAllByIdIn(List<Long> ids);

    @Query("SELECT bc FROM BookCategory bc JOIN FETCH bc.book b JOIN FETCH bc.category c WHERE bc.id = :categoryId")
    List<BookCategory> findAllByCategoryIdWithBook(@Param("categoryId") long categoryId);
}
