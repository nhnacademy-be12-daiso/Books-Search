package com.daisobook.shop.booksearch.BooksSearch.controller;

import com.daisobook.shop.booksearch.BooksSearch.dto.coupon.response.BookCategoryResponse;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.BookIdListReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookGroupReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookMetadataReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.DeleteBookReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.order.BookReviewRequest;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.BookListRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.BookRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.HomeBookListRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.order.BookReviewResponse;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.order.OrderBooksInfoRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.service.book.BookService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

//    @PostMapping
//    public ResponseEntity addBook(@RequestBody BookReqDTO bookReqDTO){
////        bookService.registerBook(bookReqDTO);
//        return ResponseEntity.ok().build();
//    }

//    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
//    public ResponseEntity addBook(@RequestPart BookMetadataReqDTO bookMetadataReqDTO) throws JsonProcessingException {
//        BookGroupReqDTO bookGroupReqDTO = bookService.parsing(bookMetadataReqDTO);
//        bookService.registerBook(bookGroupReqDTO.bookReqDTO(), bookGroupReqDTO.fileMap());
//        return ResponseEntity.ok().build();
//    }
//
//    @PostMapping("/batch")
//    public ResponseEntity addBooks(@RequestBody List<BookReqDTO> bookReqDTOs){
//        bookService.registerBooks(bookReqDTOs);
//        return ResponseEntity.ok().build();
//    }
//
//    @GetMapping("/home")
//    public HomeBookListRespDTO getHome(@RequestHeader(value = "X-User-Id", required = false)Long userId){
//        return bookService.getHomeBookLists(userId);
//    }
//
//    @GetMapping("{bookId}")
//    public BookRespDTO getBookId(@PathVariable("bookId") long bookId,
//                                 @RequestHeader(value = "X-User-Id", required = false)Long userId){
//        return bookService.findBookById(bookId, userId);
//    }
//
//    @GetMapping("/ISBN-search/{bookIsbn}")
//    public BookRespDTO getBookIsbn(@PathVariable("bookIsbn") String bookIsbn,
//                                   @RequestHeader(value = "X-User-Id", required = false)Long userId){
//        return bookService.findBookByIsbn(bookIsbn, userId);
//    }
//
//    @GetMapping("/lists")
//    public HomeBookListRespDTO getHomeBookLists(@RequestHeader(value = "X-User-Id", required = false)Long userId){
//        HomeBookListRespDTO homeBookLists = bookService.getHomeBookLists(null);
//        return homeBookLists;
//    }
//
//    @PostMapping("/list")
//    public List<BookListRespDTO> getBookList(@RequestBody BookIdListReqDTO bookIdListReqDTO){
//        List<Long> bookIds = bookIdListReqDTO.bookIdList();
//        return bookService.getBooksByIdIn(bookIds);
//    }
//
//    @PostMapping("/list/book-review")
//    List<BookReviewResponse> getBookReviewList(@RequestBody BookReviewRequest bookReviewRequest){
//        return bookService.getBooksByIdIn_ReviewId(bookReviewRequest.userId(), bookReviewRequest.bookOrderDetailRequests());
//    }
//
//    @GetMapping
//    public List<BookRespDTO> getBooks(@RequestParam(value = "categoryName", required = false) String categoryName,
//                                      @RequestParam(value = "tagName", required = false) String tagName,
//                                      @RequestParam(value = "author", required = false) String author,
//                                      @RequestParam(value = "publisher", required = false) String publisher){
//        return bookService.findBooks(categoryName, tagName, author, publisher);
//    }
//
////    @PatchMapping("{bookId}")
////    public ResponseEntity modifyBook(@PathVariable("bookId") long bookId,
////                                     @RequestBody BookReqDTO bookReqDTO){
//////        bookService.updateBook(bookId, bookReqDTO);
////        return ResponseEntity.ok().build();
////    }
//
//
//    @PatchMapping(value = "{bookId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
//    public ResponseEntity modifyBook(@PathVariable("bookId") long bookId,
//                                     @RequestPart BookMetadataReqDTO bookMetadataReqDTO) throws JsonProcessingException {
//        BookGroupReqDTO bookGroupReqDTO = bookService.parsing(bookMetadataReqDTO);
//        bookService.registerBook(bookGroupReqDTO.bookReqDTO(), bookGroupReqDTO.fileMap());
//        return ResponseEntity.ok().build();
//    }
//
//    @DeleteMapping
//    public ResponseEntity deleteBook(@RequestBody DeleteBookReqDTO deleteBookReqDTO){
//        bookService.deleteBook(deleteBookReqDTO);
//        return ResponseEntity.ok().build();
//    }

    @GetMapping("{bookId}/category")
    public BookCategoryResponse getBookCategory(@PathVariable Long bookId){
        return bookService.bookcategory(bookId);

    }

//    @PostMapping("/order-service/books")
//    public OrderBooksInfoRespDTO getOrderBookInfoList(@RequestBody BookIdListReqDTO bookIdListReqDTO){
//        return bookService.findBooksByIdIn(bookIdListReqDTO.bookIdList());
//    }
}
