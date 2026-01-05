package com.daisobook.shop.booksearch.books_search.mapper.review.impl;

import com.daisobook.shop.booksearch.books_search.dto.projection.BookReviewProjection;
import com.daisobook.shop.booksearch.books_search.dto.response.ImageRespDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.ReviewRespDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.order.BookResponse;
import com.daisobook.shop.booksearch.books_search.dto.response.order.BookReviewResponse;
import com.daisobook.shop.booksearch.books_search.entity.review.Review;
import com.daisobook.shop.booksearch.books_search.mapper.image.ImageMapper;
import com.daisobook.shop.booksearch.books_search.mapper.review.ReviewMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class ReviewMapperImpl implements ReviewMapper {
    private final ImageMapper imageMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<ReviewRespDTO> toReviewRespDTOList(List<Review> reviews) {
        if(reviews == null || reviews.isEmpty()){
            return null;
        }

        Map<Long, List<ImageRespDTO>> imagesRespDTOList = imageMapper.toImagesRespDTOListMap(reviews);
        Set<Long> keySet = imagesRespDTOList.keySet();

        return reviews.stream()
                .map(r ->
                        new ReviewRespDTO(r.getId(), r.getBook().getId(), r.getBook().getTitle(),
                        r.getUserId(), r.getContent(), r.getRating(), r.getCreatedAt(), r.getModifiedAt(),
                        keySet.contains(r.getId()) ? imagesRespDTOList.get(r.getId()): null))
                .toList();
    }

    @Override
    public List<ReviewRespDTO> toReviewRespDTOList(String reviewsData) throws JsonProcessingException {
        if(reviewsData == null || reviewsData.isBlank()){
            return null;
        }

        return objectMapper.readValue(reviewsData, new TypeReference<List<ReviewRespDTO>>() {});
    }

    @Override
    public List<BookReviewResponse> toBookReviewResponseList(List<BookReviewProjection> bookReviewProjectionList) throws JsonProcessingException {
        Map<Long, BookResponse> bookResponseMap = new HashMap<>();
        for(BookReviewProjection b: bookReviewProjectionList) {
            BookResponse bookResponse = new BookResponse(b.getBookId(), b.getTitle(), imageMapper.toImageRespDTOList(b.getImages()));
            bookResponseMap.put(b.getBookId(), bookResponse);
        }

        return bookReviewProjectionList.stream()
                .map(b ->
                        new BookReviewResponse(bookResponseMap.getOrDefault(b.getBookId(), null),
                                b.getOrderDetailId(), b.getReviewId()
                                )
                )
                .toList();
    }
}
