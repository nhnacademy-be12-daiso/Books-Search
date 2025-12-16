package com.daisobook.shop.booksearch.BooksSearch.mapper.book.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.BookUpdateData;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookGroupReqV2DTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookReqV2DTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.order.OrderBookInfoRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.order.OrderBooksInfoRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.mapper.book.BookMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class BookMapperImpl implements BookMapper {
    private final ObjectMapper objectMapper;

    @Override
    public BookGroupReqV2DTO parsing(String metadataJson, MultipartFile image0, MultipartFile image1,
                                     MultipartFile image2, MultipartFile image3, MultipartFile image4) throws JsonProcessingException {

        BookReqV2DTO metadata = objectMapper.readValue(metadataJson, BookReqV2DTO.class);
        Map<String, MultipartFile> files = new HashMap<>();

        if(image0 != null){
            files.put(image0.getName(), image0);
        }
        if(image1 != null){
            files.put(image1.getName(), image1);
        }
        if(image2 != null){
            files.put(image2.getName(), image2);
        }
        if(image3 != null){
            files.put(image3.getName(), image1);
        }
        if(image4 != null){
            files.put(image4.getName(), image2);
        }

        return new BookGroupReqV2DTO(metadata, files);
    }

    @Override
    public Book create(BookReqV2DTO req) {
        Book book = new Book(req.isbn(), req.title(), req.index(), req.description(), req.publicationDate(),
                req.price(), req.isPackaging(), req.stock(), req.status(), req.volumeNo());

        if(req.isDeleted()){
            book.setDeleted(true);
        }

        return book;
    }

    @Override
    public BookUpdateData toBookUpdateData(BookReqV2DTO req){
        return new BookUpdateData(req.title(), req.index(), req.description(), req.authorReqDTOList(), req.publisher(),
                req.publicationDate(), req.price(), req.isPackaging(), req.stock(), req.status(), req.volumeNo(), req.categoryId(),
                req.tagNameList(),req.isDeleted());
    }

    @Override
    public OrderBooksInfoRespDTO toOrderBookInfoRespDTOList(List<Book> bookList, Map<Long, Long> discountPriceMap){

        return new OrderBooksInfoRespDTO(bookList.stream()
                .map(b ->
                        new OrderBookInfoRespDTO(b.getId(), b.getTitle(), b.getPrice(), b.getStock(),
                                discountPriceMap.containsKey(b.getId()) ? BigDecimal.valueOf((double) discountPriceMap.get(b.getId()) / b.getPrice() * 100.0) : null,
                                discountPriceMap.getOrDefault(b.getId(), null), b.getBookImages() != null ? b.getBookImages().getFirst() != null ? b.getBookImages().getFirst().getPath() : null : null))
                .toList());
    }
}
