package com.daisobook.shop.booksearch.BooksSearch.mapper.image.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.ImageMetadataReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.service.ImagesReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.mapper.image.ImageMapper;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ImageMapperImpl implements ImageMapper {
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
}
