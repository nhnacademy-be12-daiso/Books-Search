package com.daisobook.shop.booksearch.BooksSearch.repository.tag;

import com.daisobook.shop.booksearch.BooksSearch.entity.tag.BookTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookTagRepository extends JpaRepository<BookTag, Long> {

    void deleteBookTagsByIdIn(List<Long> ids);

    List<BookTag> findAllByBook_IdAndTag_IdIn(long bookId, List<Long> tagsId);

    boolean existsByTag_Id(long tagId);

}
