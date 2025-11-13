package com.daisobook.shop.booksearch.BooksSearch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name ="Book_Tags")
public class BookTag {

    @Id
    @Column(name="book_tag_id")
    private long id;

    @Column(name="book_id")
    private long bookId;

    @Column(name="tag_id")
    private long tagId;
}
