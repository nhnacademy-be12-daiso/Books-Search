package com.daisobook.shop.booksearch.entity.book;

import com.daisobook.shop.booksearch.dto.request.book.BookReqDTO;
import com.daisobook.shop.booksearch.entity.author.BookAuthor;
import com.daisobook.shop.booksearch.entity.category.BookCategory;
import com.daisobook.shop.booksearch.entity.publisher.Publisher;
import com.daisobook.shop.booksearch.entity.review.Review;
import com.daisobook.shop.booksearch.entity.tag.BookTag;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private long id;

    @Setter
    @Column(name="isbn", nullable = false, length = 17, unique = true)
    private String isbn;

    @Setter
    @Column(name="title", nullable = false, length = 300)
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
    private Long price;

    @Setter
    @Column(name="is_packaging")
    private boolean isPackaging;

    @Setter
    @Column(name="stock", nullable = false)
    private Integer stock;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name="status", length = 20, nullable = false)
    private Status status;

//    @Setter
//    @Column(name = "image_url")
//    private String imageUrl;

    @Setter
    @Column(name = "volume_no")
    private Integer volumeNo;

    @Setter
    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Setter
    @Column(name = "review_summary")
    private String reviewSummary;

    public Book(String isbn, String title, String index, String description/*, String author*//*, String publisher*/,
                LocalDate publicationDate, Long price, boolean isPackaging, Integer stock, Status status,
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
    @BatchSize(size = 100)
    private List<BookCategory> bookCategories;

    @Setter
    @OneToMany(mappedBy = "book")
    @BatchSize(size = 100)
    private List<BookTag> bookTags;

    @Setter
    @ManyToOne
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;

    @Setter
    @OneToMany(mappedBy = "book")
    @BatchSize(size = 100)
    private List<BookAuthor> bookAuthors;

    @Setter
    @OneToMany(mappedBy = "book")
    @BatchSize(size = 100)
    private List<BookImage> bookImages;

    @Setter
    @OneToMany(mappedBy = "book")
    @BatchSize(size = 100)
    private List<Review> reviews;
}
