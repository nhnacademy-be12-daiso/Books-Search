package com.daisobook.shop.booksearch.controller.external;

import com.daisobook.shop.booksearch.controller.docs.LikeV2ControllerDocs;
import com.daisobook.shop.booksearch.dto.response.like.MyLikeList;
import com.daisobook.shop.booksearch.service.like.impl.LikeFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class LikeV2Controller implements LikeV2ControllerDocs {
    private final LikeFacade likeFacade;

    /**
     * 특정 도서에 좋아요 추가
     */
    @PostMapping("/api/v2/books/{bookId}/likes")
    public ResponseEntity<Void> addLike(@PathVariable("bookId") long bookId,
                                  @RequestHeader(value = "X-User-Id") long userId){
        likeFacade.addLike(bookId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 내가 좋아요한 도서 목록 조회
     */
    @GetMapping("/api/v2/likes/me")
    public MyLikeList getMyLikeList(@RequestHeader(value = "X-User-Id") long userId){
        return likeFacade.getMyLikeList(userId);
    }

    /**
     * 특정 도서의 좋아요 취소
     */
    @DeleteMapping("/api/v2/books/{bookId}/likes")
    public ResponseEntity deleteLike(@PathVariable("bookId") long bookId,
                                     @RequestHeader("X-User-Id") long userId){
        likeFacade.deleteLike(bookId, userId);
        return ResponseEntity.noContent().build();
    }
}
