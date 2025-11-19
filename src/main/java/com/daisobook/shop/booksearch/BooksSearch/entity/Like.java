package com.daisobook.shop.booksearch.BooksSearch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Entity
@Table(name="Likes", uniqueConstraints = {@UniqueConstraint(columnNames = {"book_id", "user_created_id"})})
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

    @Setter
    @Column(name = "title")
    private String title;

    @Setter
    @Column(name = "image_url")
    private String imageUrl;

    public Like(Book book, long userId, String title, String imageUrl){
        this.book = book;
        this.userId = userId;
        this.title = title;
        this.imageUrl = imageUrl;
    }
}
