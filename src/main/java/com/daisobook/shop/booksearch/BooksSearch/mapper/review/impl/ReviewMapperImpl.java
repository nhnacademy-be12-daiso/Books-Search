package com.daisobook.shop.booksearch.BooksSearch.mapper.review.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.response.ImageRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.ReviewRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.review.Review;
import com.daisobook.shop.booksearch.BooksSearch.mapper.image.ImageMapper;
import com.daisobook.shop.booksearch.BooksSearch.mapper.review.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class ReviewMapperImpl implements ReviewMapper {
    private final ImageMapper imageMapper;

    @Override
    public List<ReviewRespDTO> toReviewRespDTOList(List<Review> reviews) {
        Map<Long, List<ImageRespDTO>> imagesRespDTOList = imageMapper.toImagesRespDTOListMap(reviews);
        Set<Long> keySet = imagesRespDTOList.keySet();

        return reviews.stream()
                .map(r ->
                        new ReviewRespDTO(r.getId(), r.getBook().getId(), r.getBook().getTitle(),
                        r.getUserId(), r.getContent(), r.getRating(), r.getCreatedAt(), r.getModifiedAt(),
                        keySet.contains(r.getId()) ? imagesRespDTOList.get(r.getId()): null))
                .toList();
    }
}
