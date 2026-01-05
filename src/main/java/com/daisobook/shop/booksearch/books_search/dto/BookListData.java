package com.daisobook.shop.booksearch.books_search.dto;

import com.daisobook.shop.booksearch.books_search.dto.response.*;
import com.daisobook.shop.booksearch.books_search.dto.response.category.CategoryRespDTO;
import com.daisobook.shop.booksearch.books_search.entity.book.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class BookListData {
    private long id;
    private String isbn;
    private String title;
    private String description;
    private List<AuthorRespDTO> authorList;
    private PublisherRespDTO publisher;
    private LocalDate publicationDate;
    private Long price;
    private BigDecimal discountPercentage;
    private Long discountPrice;
    private Status status;
    private List<ImageRespDTO> imageList;
    private List<CategoryRespDTO> categoryList;
    private List<TagRespDTO> tagList;
    private Integer volumeNo;
    private Boolean isPackaging;
    private boolean isDeleted;
}
