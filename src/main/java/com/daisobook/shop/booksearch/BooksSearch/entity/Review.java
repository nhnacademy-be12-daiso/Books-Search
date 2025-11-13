package com.daisobook.shop.booksearch.BooksSearch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Getter
@Table(name="Reviews")
public class Review {

    @Id
    @Column(name="review_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Setter
    @Column(name="book_id")
    private long bookId;

    @Setter
    @Column(name="user_created_id")
    private long userId;

    @Setter
    @Column(name="content")
    private String content;

    @Setter
    @Column(name="rating")
    private int rating;

    @Setter
    @Column(name="created_at")
    private ZonedDateTime createdAt;

    @Setter
    @Column(name="modified_at")
    private ZonedDateTime modifiedAt;
}
