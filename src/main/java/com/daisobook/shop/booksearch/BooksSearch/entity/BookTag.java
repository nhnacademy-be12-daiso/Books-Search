package com.daisobook.shop.booksearch.BooksSearch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Entity
@Getter
@Table(name ="Book_Tags", uniqueConstraints = {@UniqueConstraint(columnNames = {"book_id", "tag_id"})})
public class BookTag {

    @Id
    @Column(name="book_tag_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

//    @Setter
//    @Column(name="book_id")
//    private long bookId;
    @Setter
    @ManyToOne
    private Book book;

//    @Setter
//    @Column(name="tag_id")
//    private long tagId;
    @Setter
    @ManyToOne
    private Tag tag;

    public BookTag(Book book, Tag tag){
        this.book = book;
        this.tag = tag;
    }
}
