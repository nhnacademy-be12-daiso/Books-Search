package com.daisobook.shop.booksearch.mapper.review;

import com.daisobook.shop.booksearch.dto.projection.BookReviewProjection;
import com.daisobook.shop.booksearch.dto.response.ReviewRespDTO;
import com.daisobook.shop.booksearch.dto.response.order.BookReviewResponse;
import com.daisobook.shop.booksearch.entity.review.Review;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface ReviewMapper {
    List<ReviewRespDTO> toReviewRespDTOList(List<Review> reviews);
    List<ReviewRespDTO> toReviewRespDTOList(String reviewsData) throws JsonProcessingException;
    List<BookReviewResponse> toBookReviewResponseList(List<BookReviewProjection> bookReviewProjectionList) throws JsonProcessingException;
}
