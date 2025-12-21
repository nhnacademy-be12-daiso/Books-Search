package com.daisobook.shop.booksearch.BooksSearch.search.component;

import com.daisobook.shop.booksearch.BooksSearch.search.domain.Book;
import com.daisobook.shop.booksearch.BooksSearch.search.dto.BookResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BookMapper {

    public BookResponseDto toDto(Book book, int matchRate) {
        BookResponseDto dto = BookResponseDto.builder()
                .id(book.getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .description(book.getDescription())
                .imageUrl(book.getImageUrl())
                .price(book.getPrice() == null ? 0 : book.getPrice())
                .categories(book.getCategories())
                .matchRate(matchRate)
                .aiResult(book.getAiResult())
                .build();

        return dto;
    }

    public List<BookResponseDto> toDtoList(List<Book> books, int defaultScore) {
        return books.stream()
                .map(b -> toDto(b, defaultScore))
                .collect(Collectors.toList());
    }
}
