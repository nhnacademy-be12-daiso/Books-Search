package com.daisobook.shop.booksearch.BooksSearch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Book_Categories")
public class BookCategory {
    @Id
    @Column(name = "book_category_id")
    private long id;

    @Column(name = "book_id")
    private long bookId;

    @Column(name = "category_id")
    private long categoryId;
}
