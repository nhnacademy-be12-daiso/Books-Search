package com.daisobook.shop.booksearch.BooksSearch.entity;

import com.daisobook.shop.booksearch.BooksSearch.dto.service.ImageDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "BookImages")
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
    @Column(name = "book_imgae_path")
    private String path;

    @Setter
    @Column(name = "image_type")
    private ImageType imageType;

    public BookImage(int no, String path, ImageType imageType){
        this.no = no;
        this.path =  path;
        this.imageType = imageType;
    }

    public static BookImage create(ImageDTO imageDTO){
        return new BookImage(imageDTO.no(), imageDTO.path(), imageDTO.imageType());
    }
}
