package com.daisobook.shop.booksearch.BooksSearch.controller;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.LikeReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.LikeRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.service.like.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/likes")
public class LikeController {

    private final LikeService likeService;

    @PostMapping
    public ResponseEntity addLike(@RequestBody LikeReqDTO likeReqDTO){
        likeService.createLike(likeReqDTO);
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

    @DeleteMapping("/{likeId}")
    public ResponseEntity deleteLike(@PathVariable("likeId") long likeId){
        likeService.deleteLike(likeId);
        return ResponseEntity.ok().build();
    }

}
