package com.daisobook.shop.booksearch.BooksSearch.repository.publisher;

import com.daisobook.shop.booksearch.BooksSearch.entity.publisher.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface PublisherRepository extends JpaRepository<Publisher, Long> {
//    boolean existsPublisherByName(String name);

    Publisher findPublisherByName(String name);

//    Publisher findPublisherById(long id);

    List<Publisher> findAllByNameIn(Collection<String> names);
}
