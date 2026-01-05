package com.daisobook.shop.booksearch.books_search.search.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;
import java.util.List;

/**
 * RabbitMQ로 전송되는 도서 정보 DTO
 * 도서 검색 관련 작업을 RabbitMQ 메시지로 처리할 때 사용됩니다.
 * 도서의 주요 속성들을 포함하고 있으며,
 * JSON 직렬화/역직렬화를 지원합니다.
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RabbitBook {

    @Id
    private long id;

    private String isbn;

    private String title;

    private String author;

    private String publisher;

    private String description;

    private List<String> categories;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate pubDate;

    private Integer price;

    @JsonProperty("image_url")
    private String imageUrl;

    private Long publisherId;

    private Long categoryId;


    public RabbitBook(Integer id,String isbn, String title, String author, String publisher,
                      String description, List<String> categories, LocalDate pubDate,
                      Integer price, String imageUrl) {
        this.id = id; // _id 필드에 isbn 할당
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.description = description;
        this.categories = categories;
        this.pubDate = pubDate;
        this.price = price;
        this.imageUrl = imageUrl;
    }
}
