package com.daisobook.shop.booksearch.books_search.search.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RabbitBookTest {

    @Test
    @DisplayName("Builder와 생성자, Getter/Setter가 정상 동작하는지 확인")
    void builderAndConstructor_settersAndGetters_work() {
        RabbitBook b1 = RabbitBook.builder()
                .id(1L)
                .isbn("ISBN-123")
                .title("제목")
                .author("저자")
                .publisher("출판사")
                .description("설명")
                .categories(List.of("A", "B"))
                .pubDate(LocalDate.of(2021, 5, 20))
                .price(15000)
                .imageUrl("http://image")
                .build();

        assertEquals(1L, b1.getId());
        assertEquals("ISBN-123", b1.getIsbn());
        assertEquals(LocalDate.of(2021, 5, 20), b1.getPubDate());
        assertEquals("http://image", b1.getImageUrl());

        // 기존 생성자(Integer id, ...) 동작 확인 (id 필드 타입은 long)
        RabbitBook b2 = new RabbitBook(42, "ISBN-42", "T", "A", "P", "D", List.of(), LocalDate.now(), 0, null);
        assertEquals(42L, b2.getId());
    }

    @Test
    @DisplayName("JSON 직렬화/역직렬화 시 필드명과 형식이 올바른지 확인")
    void jsonSerialization_and_deserialization_preserveFields_and_formats() throws Exception {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        RabbitBook book = RabbitBook.builder()
                .id(7L)
                .isbn("ISBN-7")
                .title("J")
                .pubDate(LocalDate.of(2020, 1, 2))
                .imageUrl("http://img")
                .build();

        String json = mapper.writeValueAsString(book);

        // pubDate는 "yyyy-MM-dd" 문자열로 직렬화되어야 하고, imageUrl은 image_url로 매핑
        assertTrue(json.contains("\"pubDate\":\"2020-01-02\""));
        assertTrue(json.contains("\"image_url\":\"http://img\""));

        // 역직렬화 테스트
        String inputJson = "{\"id\":7,\"isbn\":\"ISBN-7\",\"title\":\"J\",\"pubDate\":\"2020-01-02\",\"image_url\":\"http://img\"}";
        RabbitBook parsed = mapper.readValue(inputJson, RabbitBook.class);

        assertEquals(7L, parsed.getId());
        assertEquals("ISBN-7", parsed.getIsbn());
        assertEquals(LocalDate.of(2020, 1, 2), parsed.getPubDate());
        assertEquals("http://img", parsed.getImageUrl());
    }
}
