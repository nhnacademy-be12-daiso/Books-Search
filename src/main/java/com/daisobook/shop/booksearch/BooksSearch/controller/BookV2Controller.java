package com.daisobook.shop.booksearch.BooksSearch.controller;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookGroupReqV2DTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.SortBookListRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.TotalDataRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.BookAdminResponseDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.BookRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.BookUpdateView;
import com.daisobook.shop.booksearch.BooksSearch.entity.BookListType;
import com.daisobook.shop.booksearch.BooksSearch.service.book.impl.BookFacade;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/books")
public class BookV2Controller {
    private final BookFacade bookFacade;

    //POST: /api/v2/books 단일 도서 등록
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity addBook(@RequestPart("metadata") String metadataJson,
                                  @RequestPart(value = "image0", required = false) MultipartFile image0,
                                  @RequestPart(value = "image1", required = false) MultipartFile image1,
                                  @RequestPart(value = "image2", required = false) MultipartFile image2,
                                  @RequestPart(value = "image3", required = false) MultipartFile image3,
                                  @RequestPart(value = "image4", required = false) MultipartFile image4) throws JsonProcessingException {
        BookGroupReqV2DTO bookGroupReqV2DTO = bookFacade.parsing(metadataJson, image0, image1, image2, image3, image4);
        bookFacade.registerBook(bookGroupReqV2DTO.bookReqDTO(), bookGroupReqV2DTO.fileMap());
        return ResponseEntity.ok().build();
    }

    //POST: /api/v2/books/batch 복수 도서 등록(csv파일 같은 것들)
    @PostMapping(value = "/batch", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity addBooks(@RequestPart(value = "bookFile", required = false) MultipartFile bookFile) throws JsonProcessingException {
        //TODO 해당 파일을 파싱 해야한다
        bookFacade.registerBooks(new ArrayList<>());
        return ResponseEntity.ok().build();
    }

    //PATCH: /api/v2/books/{bookId} 단일 도서 수정
    @PatchMapping(value = "/{bookId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity motifyBook(@PathVariable("bookId") long bookId,
                                     @RequestPart("metadata") String metadataJson,
                                     @RequestPart(value = "image0", required = false) MultipartFile image0,
                                     @RequestPart(value = "image1", required = false) MultipartFile image1,
                                     @RequestPart(value = "image2", required = false) MultipartFile image2,
                                     @RequestPart(value = "image3", required = false) MultipartFile image3,
                                     @RequestPart(value = "image4", required = false) MultipartFile image4) throws JsonProcessingException {
        BookGroupReqV2DTO bookGroupReqV2DTO = bookFacade.parsing(metadataJson, image0, image1, image2, image3, image4);
        bookFacade.updateBook(bookId, bookGroupReqV2DTO.bookReqDTO(), bookGroupReqV2DTO.fileMap());
        return ResponseEntity.ok().build();
    }

    //DELETE: /api/v2/books/{bookID} 단일 도서 삭제 (도서 id를 통한)
    @DeleteMapping("/{bookId}")
    public ResponseEntity deleteBookById(@PathVariable("bookId") long bookId){
        bookFacade.deleteBookById(bookId);
        return ResponseEntity.ok().build();
    }

    //DELETE: /api/v2/books?isbn="" 단일 도서 삭제 (도서 isbn을 통한)
    @DeleteMapping("/isbn/{isbn}")
    public ResponseEntity deleteBookByIsbn(@PathVariable("isbn") String isbn){
        bookFacade.deleteBookByIsbn(isbn);
        return ResponseEntity.ok().build();
    }

    //GET: /api/v2/books?list-type="" 해당 도서 리스트 조회 (예: 베스트 셀러, 신간도서 등)
    @GetMapping
    public SortBookListRespDTO getBookListBySort(@RequestParam("type") BookListType listType,
                                  @RequestHeader(value = "X-User-Id", required = false)Long userId){
        return bookFacade.getBookList(listType, userId);
    }

    //GET: /api/v2/books/{bookId} 해당 도서 조회
    @GetMapping("/{bookId}")
    public BookRespDTO getBookDetail(@PathVariable("bookId") long bookId,
                                     @RequestHeader(value = "X-User-Id", required = false)Long userId){
        return bookFacade.getBookDetail(bookId, userId);
    }

    //GET: /api/v2/books/{bookId} 해당 도서 조회
    @GetMapping("/{bookId}/modify")
    public BookUpdateView getBookUpdateView(@PathVariable("bookId") long bookId){
        return bookFacade.getBookUpdateView(bookId);
    }

    @GetMapping("/admin-book-list")
    public Page<BookAdminResponseDTO> findAllForAdmin(@PageableDefault(size = 15, sort = "publication_date", direction = Sort.Direction.DESC) Pageable pageable){
        return bookFacade.findAllForAdmin(pageable);
    }

    @GetMapping("/admin-total-info")
    public TotalDataRespDTO getTotalData(){
        return bookFacade.getTotalDate();
    }

    @GetMapping("/isbn-search/{isbn}")
    public boolean getBookRegisterInfoByIsbn(@PathVariable("isbn") String isbn){
        return bookFacade.existIsbn(isbn);
    }

    @PostMapping("/isbn-search/{isbn}")
    public BookRespDTO postBookRegisterInfoByIsbn(@PathVariable("isbn") String isbn){
        return null;
    }
}
