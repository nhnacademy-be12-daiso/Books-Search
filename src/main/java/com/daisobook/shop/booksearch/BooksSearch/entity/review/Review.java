package com.daisobook.shop.booksearch.BooksSearch.entity.review;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.review.ReviewReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Entity
@Getter
@Table(name="reviews", uniqueConstraints = {@UniqueConstraint(columnNames = {"book_id", "user_created_id", "oder_detail_id"})})
public class Review {

    @Id
    @Column(name="review_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

//    @Setter
//    @Column(name="book_id")
//    private long bookId;
    @Setter
    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    @Setter
    @Column(name="user_created_id")
    private long userId;

    @Setter
    @Column(name = "order_detail_id")
    private long oderDetailId;

    @Setter
    @Column(name="content")
    private String content;

    @Setter
    @Column(name="rating")
    private int rating;

    @Setter
    @OneToMany(mappedBy = "review")
    @BatchSize(size = 100)
    private List<ReviewImage> reviewImages;

    @Setter
    @Column(name="created_at")
    private ZonedDateTime createdAt;

    @Setter
    @Column(name="modified_at")
    private ZonedDateTime modifiedAt;

    public Review(Book book, long userId, long oderDetailId, String content, int rating){
        this.book = book;
        this.userId = userId;
        this.oderDetailId = oderDetailId;
        this.content = content;
        this.rating = rating;
        this.createdAt = ZonedDateTime.now();

        this.reviewImages = new ArrayList<>();
    }

    public static Review create(ReviewReqDTO dto, Book book){
        return new Review(book, dto.userId(), dto.orderDetailId(), dto.content(), dto.rating());
    }
}
