package com.daisobook.shop.booksearch.BooksSearch.service.review;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.review.ReviewGroupReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.review.ReviewMetadataReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.review.ReviewReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.ReviewRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.Book;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ReviewService {
    ReviewGroupReqDTO parsing(ReviewMetadataReqDTO dto) throws JsonProcessingException;
    void registerReview(ReviewReqDTO reviewReqDTO, Map<String, MultipartFile> fileMap, Book book);
    ReviewRespDTO getReviewById(long id);
    List<ReviewRespDTO> getReviewsByUserId(long userId);
    List<ReviewRespDTO> getReviewsByBookId(long bookId);
    void updateReview(long reviewId, ReviewReqDTO reviewReqDTO, Map<String , MultipartFile> fileMap);
}
