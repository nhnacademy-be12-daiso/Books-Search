package com.daisobook.shop.booksearch.BooksSearch.controller.external;

import com.daisobook.shop.booksearch.BooksSearch.controller.docs.LikeV2ControllerDocs;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.like.MyLikeList;
import com.daisobook.shop.booksearch.BooksSearch.service.like.impl.LikeFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class LikeV2Controller implements LikeV2ControllerDocs {
    private final LikeFacade likeFacade;

    @PostMapping("/api/v2/books/{bookId}/likes")
    public ResponseEntity addLike(@PathVariable("bookId") long bookId,
                                  @RequestHeader(value = "X-User-Id") long userId){
        likeFacade.addLike(bookId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/v2/likes/me")
    public MyLikeList getMyLikeList(@RequestHeader(value = "X-User-Id") long userId){
        return likeFacade.getMyLikeList(userId);
    }

    @DeleteMapping("/api/v2/books/{bookId}/likes")
    public ResponseEntity deleteLike(@PathVariable("bookId") long bookId,
                                     @RequestHeader("X-User-Id") long userId){
        likeFacade.deleteLike(bookId, userId);
        return ResponseEntity.ok().build();
    }
}
