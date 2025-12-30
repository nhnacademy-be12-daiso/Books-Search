package com.daisobook.shop.booksearch.BooksSearch.entity.category;

import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Entity
@Getter
@Table(name = "book_categories", uniqueConstraints = @UniqueConstraint(columnNames = {"book_id"}))
public class BookCategory {
    @Id
    @Column(name = "book_category_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

//    @Setter
//    @Column(name = "book_id")
//    private long bookId;
    @Setter
    @OneToOne
    @JoinColumn(name = "book_id", nullable = false, unique = true)
    private Book book;

//    @Setter
//    @Column(name = "category_id")
//    private long categoryId;
    @Setter
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    public BookCategory(Book book, Category category){
        if(book == null || category == null){
            throw new IllegalArgumentException("null 값이 존재 ");
        }

        this.book = book;
        this.category = category;
    }

}
