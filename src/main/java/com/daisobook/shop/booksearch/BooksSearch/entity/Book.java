package com.daisobook.shop.booksearch.BooksSearch.entity;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookReqDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "Books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private long id;

    @Setter
    @Column(name="isbn", nullable = false, length = 17, unique = true)
    private String isbn;

    @Setter
    @Column(name="title", nullable = false, length = 100)
    private String title;

    @Setter
    @Column(name="indexs", columnDefinition = "TEXT")
    private String index;

    @Setter
    @Column(name="description", columnDefinition = "TEXT")
    private String description;

//    @Setter
//    @Column(name="author", length = 60)
//    private String author;

//    @Setter
//    @Column(name="publisher", length = 60)
//    private String publisher;

    @Setter
    @Column(name="publication_date")
    private LocalDate publicationDate;

    @Setter
    @Column(name="price", nullable = false)
    private Integer price;

    @Setter
    @Column(name="is_packaging")
    private boolean isPackaging;

    @Setter
    @Column(name="stock", nullable = false)
    private Integer stock;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name="status", columnDefinition = "ENUM('DISCONTINUE', 'ON_SALE') DEFAULT 'ON_SALE'")
    private Status status;

//    @Setter
//    @Column(name = "image_url")
//    private String imageUrl;

    @Setter
    @Column(name = "volume_no")
    private Integer volumeNo;

    public Book(String isbn, String title, String index, String description/*, String author*//*, String publisher*/,
                LocalDate publicationDate, Integer price, boolean isPackaging, Integer stock, Status status,
                /*String imageUrl,*/ Integer volumeNo){
        this.isbn = isbn;
        this.title = title;
        this.index = index;
        this.description = description;
//        this.author = author;
//        this.publisher = publisher;
        this.publicationDate = publicationDate;
        this.price = price;
        this.isPackaging = isPackaging;
        this.stock = stock;
        this.status = status;
//        this.imageUrl = imageUrl;
        this.volumeNo = volumeNo;

        this.bookCategories = new ArrayList<>();
        this.bookTags = new ArrayList<>();
        this.bookAuthors = new ArrayList<>();
        this.bookImages = new ArrayList<>();
        this.reviews = new ArrayList<>();
    }

    public static Book create(BookReqDTO dto, Publisher publisher){
        Book newBook = new Book(dto.isbn(), dto.title(), dto.index(), dto.description()/*, dto.author()*//*, dto.publisher()*/,
                dto.publicationDate(), dto.price(), dto.isPackaging(), dto.stock(), dto.status()/*, dto.imageUrl()*/, dto.volumeNo());
        newBook.setPublisher(publisher);
        return newBook;
    }

    @Setter
    @OneToMany(mappedBy = "book")
    private List<BookCategory> bookCategories;

    @Setter
    @OneToMany(mappedBy = "book")
    private List<BookTag> bookTags;

    @Setter
    @ManyToOne
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;

    @Setter
    @OneToMany(mappedBy = "book")
    private List<BookAuthor> bookAuthors;

    @Setter
    @OneToMany(mappedBy = "book")
    private List<BookImage> bookImages;

    @Setter
    @OneToMany(mappedBy = "book")
    private List<Review> reviews;
}
