package com.daisobook.shop.booksearch.BooksSearch.service.book.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookGroupReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookMetadataReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.mapper.book.BookMapper;
import com.daisobook.shop.booksearch.BooksSearch.service.category.CategoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookFacade {
    private final BookCoreService bookCoreService;
    private final CategoryService categoryService;

    private final BookMapper bookMapper;

    public BookGroupReqDTO parsing(BookMetadataReqDTO dto) throws JsonProcessingException {
        if(dto == null){
            throw new RuntimeException("null");
        }

        return bookMapper.parsing(dto);
    }

    public void registerBook(BookReqDTO bookReqDTO, Map<String, MultipartFile> fileMap){

    }

}
