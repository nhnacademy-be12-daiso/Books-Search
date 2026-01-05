package com.daisobook.shop.booksearch.books_search.repository.tag;

import com.daisobook.shop.booksearch.books_search.entity.tag.BookTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookTagRepository extends JpaRepository<BookTag, Long> {

//    void deleteBookTagsByIdIn(List<Long> ids);

    List<BookTag> findAllByBook_IdAndTag_IdIn(long bookId, List<Long> tagsId);

//    boolean existsByTag_Id(long tagId);

}
