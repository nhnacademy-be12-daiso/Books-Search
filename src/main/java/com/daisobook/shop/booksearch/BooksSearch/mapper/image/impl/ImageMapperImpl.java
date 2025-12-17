package com.daisobook.shop.booksearch.BooksSearch.mapper.image.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.ImageMetadataReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.ImageRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.service.ImagesReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.BookImage;
import com.daisobook.shop.booksearch.BooksSearch.entity.review.Review;
import com.daisobook.shop.booksearch.BooksSearch.mapper.image.ImageMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ImageMapperImpl implements ImageMapper {
    private final ObjectMapper objectMapper;

    @Override
    public ImagesReqDTO createImagesReqDTO(Long connectId, List<ImageMetadataReqDTO> dto) {
        return new ImagesReqDTO(connectId, dto);
    }

    @Override
    public List<ImagesReqDTO> createImagesReqDTOList(Map<String, Book> bookMap, Map<String, List<ImageMetadataReqDTO>> imageListMap) {
        Set<String> isbns = bookMap.keySet();

        List<ImagesReqDTO> imagesReqDTOMap = new ArrayList<>();
        for(String isbn: isbns){
            Book book = bookMap.get(isbn);
            List<ImageMetadataReqDTO> dto = imageListMap.get(isbn);

            imagesReqDTOMap.add(new ImagesReqDTO(book.getId(), dto));
        }

        return imagesReqDTOMap;
    }

    @Override
    public List<ImageRespDTO> toImageRespDTOList(List<BookImage> bookImages) {
        return bookImages.stream()
                .map(bi -> new ImageRespDTO(bi.getNo(), bi.getPath(), bi.getImageType()))
                .toList();
    }

    @Override
    public Map<Long, List<ImageRespDTO>> toImagesRespDTOListMap(List<Review> reviews) {
        Map<Long, List<ImageRespDTO>> reviewImages = reviews.stream()
                .collect(Collectors.toMap(Review::getId,
                        r -> r.getReviewImages() != null ? r.getReviewImages().stream()
                                .map(ri ->
                                        new ImageRespDTO(ri.getNo(), ri.getPath(), ri.getImageType()))
                                .toList() : List.of()));

        return reviewImages;
    }

    @Override
    public Map<Long, List<ImageRespDTO>> toIageRespDTOMap(Map<Long, String> imageDataMap) throws JsonProcessingException {
        Set<Long> keySet = imageDataMap.keySet();

        Map<Long, List<ImageRespDTO>> listMap = new HashMap<>();
        for(Long key: keySet){
            String s = imageDataMap.get(key);
            List<ImageRespDTO> imageRespDTOList = objectMapper.readValue(s, new TypeReference<List<ImageRespDTO>>() {});
            listMap.put(key, imageRespDTOList);
        }
        return listMap;
    }
}
