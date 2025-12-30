package com.daisobook.shop.booksearch.BooksSearch.search.component;


import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.search.domain.RabbitBook;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class BookSearchPayloadMapper {

    public RabbitBook toRabbitBook(Book book) {
        RabbitBook rb = new RabbitBook();

        rb.setId(book.getId());
        rb.setIsbn(book.getIsbn());
        rb.setTitle(book.getTitle());
        rb.setAuthor(book.getBookAuthors().stream()
                .map(ba -> ba.getAuthor().getName())
                .reduce((a, b) -> a + ", " + b)
                .orElse(""));
        rb.setPublisher(book.getPublisher() != null ? book.getPublisher().getName() : "");
        rb.setDescription(book.getDescription());
//        rb.setCategories(book.getBookCategories().stream()
//                .map(bc -> bc.getCategory().getName())
//                .toList());
        rb.setPubDate(book.getPublicationDate() != null ? LocalDate.parse(book.getPublicationDate().toString()) : null);
        rb.setPrice(Math.toIntExact(book.getPrice()));
        rb.setImageUrl(book.getBookImages().isEmpty() ? "" : book.getBookImages().get(0).getPath());
        rb.setPublisherId(book.getPublisher() != null ? book.getPublisher().getId() : null);
//        rb.setCategoryId(book.getBookCategories().getLast().getCategory().getId());
        rb.setCategoryId(book.getBookCategory().getCategory().getId());


        return rb;
    }
}

