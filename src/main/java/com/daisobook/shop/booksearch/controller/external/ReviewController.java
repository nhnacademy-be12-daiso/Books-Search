package com.daisobook.shop.booksearch.controller.external;

import com.daisobook.shop.booksearch.controller.docs.ReviewControllerDocs;
import com.daisobook.shop.booksearch.dto.point.PointPolicyType;
import com.daisobook.shop.booksearch.dto.request.review.ReviewGroupReqDTO;
import com.daisobook.shop.booksearch.dto.response.ReviewRespDTO;
import com.daisobook.shop.booksearch.service.book.impl.BookFacade;
import com.daisobook.shop.booksearch.service.point.PointService;
import com.daisobook.shop.booksearch.service.review.ReviewService;
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
    private final BookFacade bookFacade;
    private final PointService pointService;

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Void> addReview(@RequestPart("metadata") String metadataJson,
                                    @RequestPart(value = "image0", required = false) MultipartFile image0,
                                    @RequestPart(value = "image1", required = false) MultipartFile image1,
                                    @RequestPart(value = "image2", required = false) MultipartFile image2) throws JsonProcessingException {
        ReviewGroupReqDTO reviewGroupReqDTO = reviewService.parsing2(metadataJson, image0, image1, image2);
        PointPolicyType type = bookFacade.registerReview(reviewGroupReqDTO.reviewReqDTO(), reviewGroupReqDTO.fileMap());
        pointService.requestReviewPoint(reviewGroupReqDTO.reviewReqDTO().userId(), type);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/me")
    public List<ReviewRespDTO> getReviewByUserId(@RequestHeader("X-User-Id") long userId){
        return reviewService.getReviewsByUserId(userId);//유저 아이디 토큰 까기
    }

    @GetMapping("/books/{bookId}")
    public List<ReviewRespDTO> getReviewByBookId(@PathVariable("bookId") long bookId){
        return reviewService.getReviewsByBookId(bookId);
    }

    @GetMapping("/{reviewId}")
    public ReviewRespDTO getReviewById(@PathVariable("reviewId") long reviewId){
        return reviewService.getReviewById(reviewId);
    }

    @PutMapping(value = "/{reviewId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Void> updateReviewById(@PathVariable("reviewId") long reviewId,
                                           @RequestPart("metadata") String metadataJson,
                                           @RequestPart(value = "image0", required = false) MultipartFile image0,
                                           @RequestPart(value = "image1", required = false) MultipartFile image1,
                                           @RequestPart(value = "image2", required = false) MultipartFile image2) throws JsonProcessingException {
        ReviewGroupReqDTO reviewGroupReqDTO = reviewService.parsing2(metadataJson, image0, image1, image2);
        reviewService.updateReview(reviewId, reviewGroupReqDTO.reviewReqDTO(), reviewGroupReqDTO.fileMap());
        return ResponseEntity.noContent().build();
    }
}
