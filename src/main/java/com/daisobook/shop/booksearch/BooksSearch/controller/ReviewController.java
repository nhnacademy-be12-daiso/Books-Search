package com.daisobook.shop.booksearch.BooksSearch.controller;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.review.ReviewGroupReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.review.ReviewMetadataReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.ReviewRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.service.review.ReviewService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity addReview(@RequestPart ReviewMetadataReqDTO reviewMetadataReqDTO) throws JsonProcessingException {
        ReviewGroupReqDTO reviewGroupReqDTO = reviewService.parsing(reviewMetadataReqDTO);
        reviewService.registerReview(reviewGroupReqDTO.reviewReqDTO(), reviewGroupReqDTO.fileMap());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public List<ReviewRespDTO> getReviewByUserId(){
        return reviewService.getReviewsByUserId(1);//유저 아이디 토큰 까기
    }

    @GetMapping("/books/{bookId}")
    public List<ReviewRespDTO> getReviewByBookId(@RequestParam("bookId") long bookId){
        return reviewService.getReviewsByBookId(bookId);
    }

    @GetMapping("/{reviewId}")
    public ReviewRespDTO getReviewById(@RequestParam("reviewId") long reviewId){
        return reviewService.getReviewById(reviewId);
    }

    @PutMapping(value = "/{reviewId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity updateReviewById(@RequestParam("reviewId") long reviewId,
                                           @RequestPart ReviewMetadataReqDTO reviewMetadataReqDTO) throws JsonProcessingException {
        ReviewGroupReqDTO reviewGroupReqDTO = reviewService.parsing(reviewMetadataReqDTO);
        reviewService.updateReview(reviewId, reviewGroupReqDTO.reviewReqDTO(), reviewGroupReqDTO.fileMap());
        return ResponseEntity.ok().build();
    }
}
