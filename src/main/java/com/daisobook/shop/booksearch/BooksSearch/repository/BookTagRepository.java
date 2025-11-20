package com.daisobook.shop.booksearch.BooksSearch.repository;

import com.daisobook.shop.booksearch.BooksSearch.entity.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.BookTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookTagRepository extends JpaRepository<BookTag, Long> {
    BookTag findAllById(long id);

    List<BookTag> findAllByBook_Id(long bookId);

    void deleteBookTagsByIdIn(List<Long> ids);

    List<BookTag> findAllByBook_IdAndTag_IdIn(long bookId, List<Long> tagsId);

    boolean existsByTag_Id(long tagId);

    List<Book> findAllByIdIn(List<Long> ids);
}
