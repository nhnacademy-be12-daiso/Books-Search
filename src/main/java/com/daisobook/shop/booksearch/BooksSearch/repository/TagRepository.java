package com.daisobook.shop.booksearch.BooksSearch.repository;

import com.daisobook.shop.booksearch.BooksSearch.entity.BookTag;
import com.daisobook.shop.booksearch.BooksSearch.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Tag findTagByName(String name);

    List<Tag> findAllByIdIn(List<Long> ids);

    List<Tag> findAllByBookTags(List<BookTag> bookTags);

    boolean existsTagByName(String name);

    boolean existsTagById(long id);

    Tag findTagById(long id);

    List<Tag> findAllByNameIn(Collection<String> names);
}
