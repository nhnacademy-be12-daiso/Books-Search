package com.daisobook.shop.booksearch.BooksSearch.controller;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.LikeReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.LikeRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.service.book.BookService;
import com.daisobook.shop.booksearch.BooksSearch.service.like.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/likes")
public class LikeController {

    private final LikeService likeService;
    private final BookService bookService;

//    @PostMapping
//    public ResponseEntity addLike(@RequestBody LikeReqDTO likeReqDTO){
//        likeService.createLike(likeReqDTO);
//        return ResponseEntity.ok().build();
//    }
    @PostMapping("/books/{bookId}")
    public ResponseEntity addLike(@PathVariable("bookId") long bookId,
                                  @RequestHeader("X-User-Id") long userId){
        bookService.addLike(bookId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/{userId}")
    public List<LikeRespDTO> getLikeList(@PathVariable("userId") long userId){
        return likeService.getLikeList(userId);
    }

    @GetMapping("/books/{bookId}")
    public int getLikeCount(@PathVariable("bookId") long bookId){
        return likeService.likeCount(bookId);
    }

    @DeleteMapping("/books/{bookId}")
    public ResponseEntity deleteLike(@PathVariable("bookId") long bookId,
                                     @RequestHeader("X-User-Id") long userId){
        bookService.deleteLike(bookId, userId);
        return ResponseEntity.ok().build();
    }

    //TODO 해당 유저가 존재하는 지 확인이 필요
    boolean check(long userId){
        //user 에 해당 유저가 있는지 확인
        return false;
    }

}
