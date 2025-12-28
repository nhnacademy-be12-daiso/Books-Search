package com.daisobook.shop.booksearch.BooksSearch.controller;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.review.ReviewGroupReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.review.ReviewMetadataReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.ReviewRespDTO;
//import com.daisobook.shop.booksearch.BooksSearch.service.book.BookService;
import com.daisobook.shop.booksearch.BooksSearch.service.book.impl.BookFacade;
import com.daisobook.shop.booksearch.BooksSearch.service.review.ReviewService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
//    private final BookService bookService;
    private final BookFacade bookFacade;

//    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
//    public ResponseEntity addReview(@RequestPart ReviewMetadataReqDTO reviewMetadataReqDTO) throws JsonProcessingException {
//        ReviewGroupReqDTO reviewGroupReqDTO = reviewService.parsing(reviewMetadataReqDTO);
////        reviewService.registerReview(reviewGroupReqDTO.reviewReqDTO(), reviewGroupReqDTO.fileMap());
//        bookService.registerReview(reviewGroupReqDTO.reviewReqDTO(), reviewGroupReqDTO.fileMap());
//        return ResponseEntity.ok().build();
//    }

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity addReview(@RequestPart("metadata") String metadataJson,
                                    @RequestPart(value = "image0", required = false) MultipartFile image0,
                                    @RequestPart(value = "image1", required = false) MultipartFile image1,
                                    @RequestPart(value = "image2", required = false) MultipartFile image2) throws JsonProcessingException {
        ReviewGroupReqDTO reviewGroupReqDTO = reviewService.parsing2(metadataJson, image0, image1, image2);
//        bookService.registerReview(reviewGroupReqDTO.reviewReqDTO(), reviewGroupReqDTO.fileMap());
        bookFacade.registerReview(reviewGroupReqDTO.reviewReqDTO(), reviewGroupReqDTO.fileMap());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public List<ReviewRespDTO> getReviewByUserId(@RequestHeader("X-User-Id") long userId){
        return reviewService.getReviewsByUserId(userId);//유저 아이디 토큰 까기
    }

    @GetMapping("/books/{bookId}")
    public List<ReviewRespDTO> getReviewByBookId(@RequestParam("bookId") long bookId){
        return reviewService.getReviewsByBookId(bookId);
    }

    @GetMapping("/{reviewId}")
    public ReviewRespDTO getReviewById(@RequestParam("reviewId") long reviewId){
        return reviewService.getReviewById(reviewId);
    }

//    @PutMapping(value = "/{reviewId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
//    public ResponseEntity updateReviewById(@RequestParam("reviewId") long reviewId,
//                                           @RequestPart ReviewMetadataReqDTO reviewMetadataReqDTO,
//                                           @RequestHeader("X-User-Id")long userId) throws JsonProcessingException {
//        ReviewGroupReqDTO reviewGroupReqDTO = reviewService.parsing(reviewMetadataReqDTO);
//        reviewService.updateReview(reviewId, reviewGroupReqDTO.reviewReqDTO(), reviewGroupReqDTO.fileMap());
//        return ResponseEntity.ok().build();
//    }

    @PutMapping(value = "/{reviewId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity updateReviewById(@RequestParam("reviewId") long reviewId,
                                           @RequestPart("metadata") String metadataJson,
                                           @RequestPart(value = "image0", required = false) MultipartFile image0,
                                           @RequestPart(value = "image1", required = false) MultipartFile image1,
                                           @RequestPart(value = "image2", required = false) MultipartFile image2) throws JsonProcessingException {
        ReviewGroupReqDTO reviewGroupReqDTO = reviewService.parsing2(metadataJson, image0, image1, image2);
        reviewService.updateReview(reviewId, reviewGroupReqDTO.reviewReqDTO(), reviewGroupReqDTO.fileMap());
        return ResponseEntity.ok().build();
    }

    //TODO 해당 리뷰 창을 요청하는 거에 대해 권한이 있는지 확인(해당 도서의 주문 디테일이 있는 사용자이며 요청을 보내는 자가 해당 사용자인지 검증이 필요)
    boolean check(long userId, long bookId, long oderDetailId){
        //주문에 보내서 체크하기
        return false;
    }
}
