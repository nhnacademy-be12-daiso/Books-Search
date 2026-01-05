package com.daisobook.shop.booksearch.repository.book;

import com.daisobook.shop.booksearch.entity.book.BookImage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookImageRepository extends JpaRepository<BookImage, Long> {
    List<BookImage> findBookImagesByBook_Id(long bookId);

    @Query("SELECT bi FROM BookImage bi WHERE bi.path LIKE 'http%' " +
            "AND bi.path NOT LIKE 'http://storage.java21.net%'")
    List<BookImage> findImagesToMigrate(Pageable pageable);
}
