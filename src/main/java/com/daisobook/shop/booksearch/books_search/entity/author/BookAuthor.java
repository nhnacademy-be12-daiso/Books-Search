package com.daisobook.shop.booksearch.books_search.entity.author;

import com.daisobook.shop.booksearch.books_search.entity.book.Book;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "book_authors", uniqueConstraints = {@UniqueConstraint(columnNames = {"book_id","author_id","role_id"})})
public class BookAuthor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_author_id")
    private long id;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    @Setter
    @ManyToOne
    @JoinColumn(name = "author_id")
    private Author author;

    @Setter
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    public BookAuthor(Book book, Author author){
        this.book = book;
        this.author = author;
    }
}
