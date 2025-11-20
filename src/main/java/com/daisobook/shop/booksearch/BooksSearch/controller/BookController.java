package com.daisobook.shop.booksearch.BooksSearch.controller;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.BookReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.DeleteBookReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.BookRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.service.book.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    @PostMapping
    public ResponseEntity addBook(@RequestBody BookReqDTO bookReqDTO){
        bookService.registerBook(bookReqDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/batch")
    public ResponseEntity addBooks(@RequestBody List<BookReqDTO> bookReqDTOs){
        bookService.registerBooks(bookReqDTOs);
        return ResponseEntity.ok().build();
    }

    @GetMapping("{bookId}")
    public BookRespDTO getBookId(@PathVariable("bookId") long bookId){
        return bookService.findBookById(bookId);
    }

    @GetMapping("/ISBN-search/{bookIsbn}")
    public BookRespDTO getBookIsbn(@PathVariable("bookIsbn") String bookIsbn){
        return bookService.findBookByIsbn(bookIsbn);
    }

    @GetMapping
    public List<BookRespDTO> getBooks(@RequestParam(value = "categoryName", required = false) String categoryName,
                                      @RequestParam(value = "tagName", required = false) String tagName,
                                      @RequestParam(value = "author", required = false) String author,
                                      @RequestParam(value = "publisher", required = false) String publisher){
        return bookService.findBooks(categoryName, tagName, author, publisher);
    }

    @PatchMapping("{bookId}")
    public ResponseEntity modifyBook(@PathVariable("bookId") long bookId,
                                     @RequestBody BookReqDTO bookReqDTO){
        bookService.updateBook(bookId, bookReqDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity deleteBook(@RequestBody DeleteBookReqDTO deleteBookReqDTO){
        bookService.deleteBook(deleteBookReqDTO);
        return ResponseEntity.ok().build();
    }
}
