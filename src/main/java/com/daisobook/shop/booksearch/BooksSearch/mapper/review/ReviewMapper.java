package com.daisobook.shop.booksearch.BooksSearch.mapper.review;

import com.daisobook.shop.booksearch.BooksSearch.dto.response.ReviewRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.review.Review;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface ReviewMapper {
    List<ReviewRespDTO> toReviewRespDTOList(List<Review> reviews);
    List<ReviewRespDTO> toReviewRespDTOList(String reviewsData) throws JsonProcessingException;
}
