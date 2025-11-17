package com.daisobook.shop.booksearch.BooksSearch.controller;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.AddBookReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.service.book.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    @PostMapping("/book")
    public ResponseEntity addBook(@RequestBody AddBookReqDTO addBookReqDTO){
        bookService.registerBook(addBookReqDTO);
        return ResponseEntity.ok().build();
    }
}
