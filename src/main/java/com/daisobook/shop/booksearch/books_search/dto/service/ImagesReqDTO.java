package com.daisobook.shop.booksearch.books_search.dto.service;

import com.daisobook.shop.booksearch.books_search.dto.request.ImageMetadataReqDTO;

import java.util.List;

public record ImagesReqDTO (
        long connectedId,
        List<ImageMetadataReqDTO> imageMetadata){
}
