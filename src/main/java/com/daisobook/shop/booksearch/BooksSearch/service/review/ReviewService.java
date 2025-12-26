package com.daisobook.shop.booksearch.BooksSearch.service.review;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.order.BookOrderDetailRequest;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.review.ReviewGroupReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.review.ReviewMetadataReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.review.ReviewReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.ReviewRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.order.BookReviewResponse;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.review.Review;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ReviewService {
    ReviewGroupReqDTO parsing(ReviewMetadataReqDTO dto) throws JsonProcessingException;
    ReviewGroupReqDTO parsing2(String metadataJson, MultipartFile image0, MultipartFile image1, MultipartFile image2) throws JsonProcessingException;
    void registerReview(ReviewReqDTO reviewReqDTO, Map<String, MultipartFile> fileMap, Book book);
    ReviewRespDTO getReviewById(long id);
    List<ReviewRespDTO> getReviewsByUserId(long userId);
    List<ReviewRespDTO> getReviewsByBookId(long bookId);
    void updateReview(long reviewId, ReviewReqDTO reviewReqDTO, Map<String , MultipartFile> fileMap);
    Review findReviewByUserIdAndBookIdAndOrderDetailId(long userId, long bookId, long orderDetailId);
    List<BookReviewResponse> findBookReviewList(Long userId, List<BookOrderDetailRequest> bookOrderDetailRequests);
    Long getCountByRelease(int day);
}
