package com.daisobook.shop.booksearch.entity.tag;

import com.daisobook.shop.booksearch.entity.book.Book;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Entity
@Getter
@Table(name ="book_tags", uniqueConstraints = {@UniqueConstraint(columnNames = {"book_id", "tag_id"})})
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
    @JoinColumn(name = "book_id")
    private Book book;

//    @Setter
//    @Column(name="tag_id")
//    private long tagId;
    @Setter
    @ManyToOne
    @JoinColumn(name = "tag_id")
    private Tag tag;

    public BookTag(Book book, Tag tag){
        this.book = book;
        this.tag = tag;
    }
}
