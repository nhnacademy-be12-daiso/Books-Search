package com.daisobook.shop.booksearch.BooksSearch.controller;

import com.daisobook.shop.booksearch.BooksSearch.service.like.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class LikeV2Controller {
    private LikeService likeService;

    @PostMapping("/api/v2/books/{bookId}/likes")
    public ResponseEntity addLike(@PathVariable("bookId") long bookId,
                                  @RequestHeader(value = "X-User-Id") long userId){
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/v2/likes/me")
    public void getMyLikeList(@RequestHeader(value = "X-User-Id") long userId){

    }

    @DeleteMapping("/api/v2/books/{bookId}/likes/{likeId}")
    public ResponseEntity deleteLike(@PathVariable("bookId") long bookId,
                                     @PathVariable("likeId") long likeId,
                                     @RequestHeader("X-User-Id") long userId){
        return ResponseEntity.ok().build();
    }
}
