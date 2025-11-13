package com.daisobook.shop.booksearch.BooksSearch.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

// Elasticsearch 인덱스에 저장된 도서 문서 구조와 일치
@Getter
@Setter
public class BookDocument {
    private Long bookId;
    private String title;
    private String author;
    private String publisher;
    private int price;
    private List<String> tags;
    private List<String> categories;
    private double averageRating;
    private int reviewCount;
    private LocalDateTime publicationDate;
    private String description;
    // ... 기타 필드 (book_vector는 내부적으로만 사용)
}