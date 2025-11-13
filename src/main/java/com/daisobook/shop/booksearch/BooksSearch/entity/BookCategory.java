package com.daisobook.shop.booksearch.BooksSearch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Table(name = "Book_Categories")
public class BookCategory {
    @Id
    @Column(name = "book_category_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Setter
    @Column(name = "book_id")
    private long bookId;

    @Setter
    @Column(name = "category_id")
    private long categoryId;
}
