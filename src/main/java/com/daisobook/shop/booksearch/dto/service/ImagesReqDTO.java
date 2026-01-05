package com.daisobook.shop.booksearch.dto.service;

import com.daisobook.shop.booksearch.dto.request.ImageMetadataReqDTO;

import java.util.List;

public record ImagesReqDTO (
        long connectedId,
        List<ImageMetadataReqDTO> imageMetadata){
}
