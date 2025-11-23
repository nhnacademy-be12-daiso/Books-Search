package com.daisobook.shop.booksearch.BooksSearch.dto.service;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.ImageMetadataReqDTO;

import java.util.List;

public record ImagesReqDTO (
        long connectedId,
        List<ImageMetadataReqDTO> imageMetadata){
}
