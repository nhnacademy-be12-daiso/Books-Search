package com.daisobook.shop.booksearch.BooksSearch.search.domain;

import com.daisobook.shop.booksearch.BooksSearch.search.dto.AiAnalysisDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDate;
import java.util.List;

/**
 * ES books 문서 매핑 모델.
 * - _id == isbn 구조(Repository.save에서 id=isbn) 기준으로 운영
 * - embedding은 응답에서 excludes 처리하므로 여기 필드가 없어도 검색 응답엔 영향 없음
 * - aiResult는 ES에 저장된 AI 분석 결과(object)를 그대로 매핑해서 내려줌
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(indexName = "books", createIndex = false, writeTypeHint = WriteTypeHint.FALSE)
public class Book {

    @Id
    private String id; // ES의 _id

    @Field(type = FieldType.Keyword)
    private String isbn;

    @Field(type = FieldType.Text, analyzer = "korean_analyzer")
    private String title;

    @Field(type = FieldType.Keyword)
    private String author;

    @Field(type = FieldType.Keyword)
    private String publisher;

    @Field(type = FieldType.Text, analyzer = "korean_analyzer")
    private String description;

    @Field(type = FieldType.Keyword)
    private List<String> categories;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate pubDate;

    @Field(type = FieldType.Integer)
    private Integer price;

    // ES 필드명(image_url)
    @Field(name = "image_url", type = FieldType.Keyword, index = false)
    @JsonProperty("image_url")
    private String imageUrl;

    // ES 필드명(aiResult) (dynamic mapping object)
    @JsonProperty("aiResult")
    private AiAnalysisDto aiResult;
}
