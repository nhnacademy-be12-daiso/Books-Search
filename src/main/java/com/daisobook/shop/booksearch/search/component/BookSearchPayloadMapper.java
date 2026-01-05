package com.daisobook.shop.booksearch.search.component;


import com.daisobook.shop.booksearch.entity.book.Book;
import com.daisobook.shop.booksearch.entity.category.BookCategory;
import com.daisobook.shop.booksearch.search.domain.RabbitBook;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

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
        rb.setCategories(book.getBookCategories().stream()
                .map(bc -> bc.getCategory().getName())
                .toList());
        rb.setPubDate(book.getPublicationDate() != null ? LocalDate.parse(book.getPublicationDate().toString()) : null);
        rb.setPrice(Math.toIntExact(book.getPrice()));
        rb.setImageUrl(book.getBookImages().isEmpty() ? "" : book.getBookImages().get(0).getPath());
        rb.setPublisherId(book.getPublisher() != null ? book.getPublisher().getId() : null);

        // 기본적으로 가장 마지막 카테고리 ID를 설정
        rb.setCategoryId(book.getBookCategories().getLast().getCategory().getId());

        // 깊이 3인 카테고리를 찾아서 설정
        List<BookCategory> categories=book.getBookCategories();
        for(BookCategory bc: categories) {
            if(bc.getCategory().getDeep()==3) {
                rb.setCategoryId(bc.getCategory().getId());
                break;
            }
        }

        return rb;
    }
}

