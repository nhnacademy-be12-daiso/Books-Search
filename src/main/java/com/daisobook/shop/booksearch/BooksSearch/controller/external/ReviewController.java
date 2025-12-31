package com.daisobook.shop.booksearch.BooksSearch.controller.external;

import com.daisobook.shop.booksearch.BooksSearch.controller.docs.ReviewControllerDocs;
import com.daisobook.shop.booksearch.BooksSearch.dto.point.PointPolicyType;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.review.ReviewGroupReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.ReviewRespDTO;
//import com.daisobook.shop.booksearch.BooksSearch.service.book.BookService;
import com.daisobook.shop.booksearch.BooksSearch.service.book.impl.BookFacade;
import com.daisobook.shop.booksearch.BooksSearch.service.point.PointService;
import com.daisobook.shop.booksearch.BooksSearch.service.review.ReviewService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reviews")
public class ReviewController implements ReviewControllerDocs {

    private final ReviewService reviewService;
//    private final BookService bookService;
    private final BookFacade bookFacade;
    private final PointService pointService;

//    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
//    public ResponseEntity addReview(@RequestPart ReviewMetadataReqDTO reviewMetadataReqDTO) throws JsonProcessingException {
//        ReviewGroupReqDTO reviewGroupReqDTO = reviewService.parsing(reviewMetadataReqDTO);
////        reviewService.registerReview(reviewGroupReqDTO.reviewReqDTO(), reviewGroupReqDTO.fileMap());
//        bookService.registerReview(reviewGroupReqDTO.reviewReqDTO(), reviewGroupReqDTO.fileMap());
//        return ResponseEntity.ok().build();
//    }

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Void> addReview(@RequestPart("metadata") String metadataJson,
                                    @RequestPart(value = "image0", required = false) MultipartFile image0,
                                    @RequestPart(value = "image1", required = false) MultipartFile image1,
                                    @RequestPart(value = "image2", required = false) MultipartFile image2) throws JsonProcessingException {
        ReviewGroupReqDTO reviewGroupReqDTO = reviewService.parsing2(metadataJson, image0, image1, image2);
//        bookService.registerReview(reviewGroupReqDTO.reviewReqDTO(), reviewGroupReqDTO.fileMap());
        PointPolicyType type = bookFacade.registerReview(reviewGroupReqDTO.reviewReqDTO(), reviewGroupReqDTO.fileMap());
        pointService.requestReviewPoint(reviewGroupReqDTO.reviewReqDTO().userId(), type);
        return ResponseEntity.status(HttpStatus.CREATED).build();
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
    public ResponseEntity<Void> updateReviewById(@RequestParam("reviewId") long reviewId,
                                           @RequestPart("metadata") String metadataJson,
                                           @RequestPart(value = "image0", required = false) MultipartFile image0,
                                           @RequestPart(value = "image1", required = false) MultipartFile image1,
                                           @RequestPart(value = "image2", required = false) MultipartFile image2) throws JsonProcessingException {
        ReviewGroupReqDTO reviewGroupReqDTO = reviewService.parsing2(metadataJson, image0, image1, image2);
        reviewService.updateReview(reviewId, reviewGroupReqDTO.reviewReqDTO(), reviewGroupReqDTO.fileMap());
        return ResponseEntity.noContent().build();
    }
}
