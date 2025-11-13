package com.daisobook.shop.booksearch.BooksSearch.entity;

import jakarta.persistence.*;

@Entity
@Table(name="Likes")
public class Like {

    @Id
    @Column(name="like_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name="book_id")
    private long bookId;

    @Column(name="user_created_id")
    private long userId;
}
