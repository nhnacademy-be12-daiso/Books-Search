package com.daisobook.shop.booksearch.BooksSearch.repository.book;

import com.daisobook.shop.booksearch.BooksSearch.entity.book.BookImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookImageRepository extends JpaRepository<BookImage, Long> {
    List<BookImage> findBookImagesByBook_Id(long bookId);
}
