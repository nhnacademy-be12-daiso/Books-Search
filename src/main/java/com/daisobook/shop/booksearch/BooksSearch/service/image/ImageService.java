package com.daisobook.shop.booksearch.BooksSearch.service.image;

import com.daisobook.shop.booksearch.BooksSearch.dto.service.ImageDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.service.ImagesReqDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ImageService {
    String getBucketName();

    Map<Integer, String> createdProcess(ImagesReqDTO imagesReqDTO, Map<String, MultipartFile> fileMap);
    Map<Integer, String> updatedProcess(ImagesReqDTO imagesReqDTO, Map<String, MultipartFile> fileMap, List<ImageDTO> imageDTOList);

    String uploadImageFromUrl(String imageUrl, Long id);
    String uploadImageFromFile(MultipartFile file, Long id);
    String updateImageFromUrl(String newImageUrl, String fullImagePath);
    String updateImageFromFile(MultipartFile newFile, String fullImagePath);
    void deleteObject(String fullImagePath);
    void deleteObjects(List<String> fullImagePathList);
}
