package com.daisobook.shop.booksearch.books_search.mapper.image;

import com.daisobook.shop.booksearch.books_search.dto.request.ImageMetadataReqDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.ImageRespDTO;
import com.daisobook.shop.booksearch.books_search.dto.service.ImagesReqDTO;
import com.daisobook.shop.booksearch.books_search.entity.book.Book;
import com.daisobook.shop.booksearch.books_search.entity.book.BookImage;
import com.daisobook.shop.booksearch.books_search.entity.review.Review;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
import java.util.Map;

public interface ImageMapper {
    ImagesReqDTO createImagesReqDTO(Long connectId, List<ImageMetadataReqDTO> dto);
    List<ImagesReqDTO> createImagesReqDTOList(Map<String, Book> bookMap, Map<String, List<ImageMetadataReqDTO>> imageListMap);
    List<ImageRespDTO> toImageRespDTOList(String imagesData) throws JsonProcessingException;
    Map<Long, List<ImageRespDTO>> toImagesRespDTOListMap(List<Review> reviews);
    Map<Long, List<ImageRespDTO>> toIageRespDTOMap(Map<Long, String> imageDataMap) throws JsonProcessingException;
}
