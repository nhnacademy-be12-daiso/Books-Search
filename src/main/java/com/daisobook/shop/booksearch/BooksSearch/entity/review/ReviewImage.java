package com.daisobook.shop.booksearch.BooksSearch.entity.review;

import com.daisobook.shop.booksearch.BooksSearch.entity.ImageType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "review_images")
public class ReviewImage {
    @Id
    @Column(name = "review_image_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "review_id")
    private Review review;

    @Setter
    @Column(name = "review_image_no")
    private int no;

    @Setter
    @Column(name = "review_image_path")
    private String path;

    @Setter
    @Column(name = "image_type")
    private ImageType imageType;

    public ReviewImage(int no, String path, ImageType imageType){
        this.no = no;
        this.path = path;
        this.imageType = imageType;
    }
}