package com.daisobook.shop.booksearch.books_search.entity.book;

import com.daisobook.shop.booksearch.books_search.entity.ImageType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "book_images")
public class BookImage {
    @Id
    @Column(name = "book_image_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    @Setter
    @Column(name = "book_image_no")
    private int no;

    @Setter
    @Column(name = "book_image_path")
    private String path;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "image_type")
    private ImageType imageType;

    public BookImage(int no, String path, ImageType imageType){
        this.no = no;
        this.path =  path;
        this.imageType = imageType;
    }

}
