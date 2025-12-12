package com.daisobook.shop.booksearch.BooksSearch.mapper.book.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookGroupReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookMetadataReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.order.OrderBookInfoRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.order.OrderBooksInfoRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.mapper.book.BookMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class BookMapperImpl implements BookMapper {
    private final ObjectMapper objectMapper;

    @Override
    public BookGroupReqDTO parsing(BookMetadataReqDTO dto) throws JsonProcessingException {
        final int MAX_FILE_COUNT = 5;

        BookReqDTO metadata = objectMapper.readValue(dto.metadata(), BookReqDTO.class);
        Map<String, MultipartFile> files = new HashMap<>();
        Class<?> clazz = dto.getClass();

        for(int i = 0; i < MAX_FILE_COUNT; i++) {
            String key = "image%d".formatted(i);
            try {
                // DTO에서 필드를 찾아 접근 권한 설정
                Field field = clazz.getDeclaredField(key);
                field.setAccessible(true);

                // DTO 인스턴스에서 해당 필드의 값(MultipartFile) 추출
                MultipartFile file = (MultipartFile) field.get(dto);

                // 파일이 비어있지 않은 경우에만 Map에 추가 (Key는 "image0", "image1"...)
                if (file != null && !file.isEmpty()) {
                    files.put(key, file);
                }
            } catch (NoSuchFieldException e) {
                // 필드가 없으면 종료
                break;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return new BookGroupReqDTO(metadata, files);
    }

    @Override
    public OrderBooksInfoRespDTO toOrderBookInfoRespDTOList(List<Book> bookList, Map<Long, Long> discountPriceMap){

        return new OrderBooksInfoRespDTO(bookList.stream()
                .map(b ->
                        new OrderBookInfoRespDTO(b.getId(), b.getTitle(), b.getPrice(), b.getStock(),
                                discountPriceMap.containsKey(b.getId()) ? BigDecimal.valueOf((double) discountPriceMap.get(b.getId()) / b.getPrice() * 100.0) : null,
                                discountPriceMap.getOrDefault(b.getId(), null), b.getBookImages() != null ? b.getBookImages().getFirst().getPath() : null))
                .toList());
    }
}
