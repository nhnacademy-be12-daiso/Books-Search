package com.daisobook.shop.booksearch.books_search.entity.like;

import com.daisobook.shop.booksearch.books_search.entity.book.Book;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@NoArgsConstructor
@Getter
@Entity
@Table(name="likes", uniqueConstraints = {@UniqueConstraint(columnNames = {"book_id", "user_created_id"})})
public class Like {

    @Id
    @Column(name="like_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name="book_id")
    private Book book;

    @Column(name="user_created_id")
    private long userId;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    public Like(Book book, long userId){
        this.book = book;
        this.userId = userId;
        this.createdAt = ZonedDateTime.now();
    }
}
