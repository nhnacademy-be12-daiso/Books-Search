package com.daisobook.shop.booksearch.BooksSearch.mapper.image;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.ImageMetadataReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.service.ImagesReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;

import java.util.List;
import java.util.Map;

public interface ImageMapper {
    ImagesReqDTO createImagesReqDTO(Long connectId, List<ImageMetadataReqDTO> dto);
    List<ImagesReqDTO> createImagesReqDTOList(Map<String, Book> bookMap, Map<String, List<ImageMetadataReqDTO>> imageListMap);
}
