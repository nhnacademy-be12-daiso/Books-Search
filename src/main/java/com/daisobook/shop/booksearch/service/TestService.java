package com.daisobook.shop.booksearch.service;

import com.daisobook.shop.booksearch.dto.test.BookCreationRequest;
import com.daisobook.shop.booksearch.dto.request.ImageMetadataReqDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class TestService {
    private final MinIOService minioService;

    public void processBookCreation(BookCreationRequest request, Map<String, MultipartFile> images) {
        long id = 6;
        for(ImageMetadataReqDTO imageDTO: request.imageMetadata()){
            String imageUrl;

            if(imageDTO.dataUrl() != null && !imageDTO.dataUrl().isEmpty()){
                imageUrl = imageDTO.dataUrl();
                minioService.uploadImageFromUrl(imageUrl, id);

            } else if (imageDTO.fileKey() != null && !imageDTO.fileKey().isEmpty()) {
                MultipartFile file = images.get(imageDTO.fileKey());

                if(file != null){
                    minioService.uploadImageFromFile(file, id);
                } else {
                    throw new RuntimeException();
                }
            } else {
                throw new RuntimeException();
            }
        }

    }
}
