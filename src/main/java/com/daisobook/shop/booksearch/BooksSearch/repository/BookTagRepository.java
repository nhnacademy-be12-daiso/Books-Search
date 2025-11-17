package com.daisobook.shop.booksearch.BooksSearch.repository;

import com.daisobook.shop.booksearch.BooksSearch.entity.BookTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookTagRepository extends JpaRepository<BookTag, Long> {
    BookTag findAllById(long id);

    List<BookTag> findAllByBook_Id(long bookId);

    void deleteBookTagsByIdIn(List<Long> ids);
}
