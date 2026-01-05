package com.daisobook.shop.booksearch.service.image.impl;

import com.daisobook.shop.booksearch.dto.request.ImageMetadataReqDTO;
import com.daisobook.shop.booksearch.dto.service.ImageDTO;
import com.daisobook.shop.booksearch.dto.service.ImagesReqDTO;
import com.daisobook.shop.booksearch.entity.ImageType;
import com.daisobook.shop.booksearch.entity.review.ReviewImage;
import com.daisobook.shop.booksearch.repository.review.ReviewImageRepository;
import com.daisobook.shop.booksearch.service.image.ImageTemplateServiceImpl;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReviewImageServiceImpl extends ImageTemplateServiceImpl {

    @Value("${minio.bucketName.review}")
    private String bucketName;

    private final int MAX_SIZE = 3;

    private final ReviewImageRepository reviewImageRepository;

    @Override
    public String getBucketName() {
        return bucketName;
    }

    public ReviewImageServiceImpl(MinioClient minioClient, WebClient.Builder webClientBuilder,
                                  ReviewImageRepository reviewImageRepository) {
        super(minioClient, webClientBuilder);
        this.reviewImageRepository = reviewImageRepository;
    }

    private List<ImageMetadataReqDTO> checkImagesCount(ImagesReqDTO imagesReqDTO){
        List<ImageMetadataReqDTO> imageDTOList = imagesReqDTO.imageMetadata();
        if(imageDTOList.size() > MAX_SIZE){
            log.error("최대 3개까지만 저장이 가능합니다 - 요청한 이미지 수:{}", imageDTOList.size());
            throw new RuntimeException("최대 5개까지만 저장이 가능합니다");
        }
        return imageDTOList;
    }

    public List<ReviewImage> addReviewImage(ImagesReqDTO imagesReqDTO, Map<String, MultipartFile> fileMap){
        List<ReviewImage> bookImages = executeAdd(imagesReqDTO, fileMap);

        reviewImageRepository.saveAll(bookImages);
        return bookImages;
    }

    public List<List<ReviewImage>> addReviewImages(List<ImagesReqDTO> imagesReqDTOs){
        List<List<ReviewImage>> reviewImagesList = new ArrayList<>();
        for(ImagesReqDTO i: imagesReqDTOs){
            List<ReviewImage> reviewImages = executeAdd(i, null);
            reviewImagesList.add(reviewImages);
        }
        List<ReviewImage> allReviewImages = reviewImagesList.stream()
                .flatMap(List::stream) // List<BookImage>의 Stream을 하나의 Stream<BookImage>으로 합침
                .collect(Collectors.toList());

        reviewImageRepository.saveAll(allReviewImages);
        return reviewImagesList;
    }

    private List<ReviewImage> executeAdd(ImagesReqDTO imagesReqDTO, Map<String, MultipartFile> fileMap){
        List<ImageMetadataReqDTO> imageDTOList = checkImagesCount(imagesReqDTO);

        Map<Integer, String> urlMap = createdProcess(imagesReqDTO, fileMap);

        List<ReviewImage> reviewImage = new ArrayList<>();
        for(Map.Entry<Integer, String> entry : urlMap.entrySet()){
            int sequence = entry.getKey();
            String url = entry.getValue();

            if(sequence >= 0 && sequence < MAX_SIZE) {
                ImageType type = getLegalImageType(sequence, imageDTOList);
                reviewImage.add(new ReviewImage(sequence, url, type));
            }
        }

        return reviewImage;
    }

    private ImageType getLegalImageType(int sequence, List<ImageMetadataReqDTO> imageDTOList){
        return imageDTOList.stream()
                .filter(i -> i.sequence() == sequence)
                .map(ImageMetadataReqDTO::type)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("이미지(Sequence: " + sequence + ")의 타입을를 찾을 수 없습니다."));
    }

    public List<ReviewImage> updateReviewImage(ImagesReqDTO imagesReqDTO, Map<String, MultipartFile> fileMap){
        checkImagesCount(imagesReqDTO);

        List<ReviewImage> existingImages = reviewImageRepository.findReviewImagesByReview_Id(imagesReqDTO.connectedId());
        Map<Integer, ReviewImage> existingImageMap = existingImages.stream()
                .collect(Collectors.toMap(ReviewImage::getNo, bi -> bi));

        List<ImageDTO> imageDTOs = existingImages.stream()
                .map(bi -> new ImageDTO(bi.getId(), bi.getReview().getId(), bi.getNo(), bi.getPath(), bi.getImageType()))
                .toList();

        Map<Integer, String> updatedUrlMap = updatedProcess(imagesReqDTO, fileMap, imageDTOs);

        Map<Integer, ImageMetadataReqDTO> requestedMetaMap = imagesReqDTO.imageMetadata().stream()
                .collect(Collectors.toMap(ImageMetadataReqDTO::sequence, dto -> dto));

        List<ReviewImage> resultImages = new ArrayList<>();
        List<ReviewImage> deleteImages = new ArrayList<>();
        for(int i = 0; i < MAX_SIZE; i++){
            ReviewImage preImage = existingImageMap.get(i);
            String updatedUrl = updatedUrlMap.get(i);
            ImageMetadataReqDTO requestedMeta = requestedMetaMap.get(i);

            if(updatedUrl != null) {
                if(preImage == null) {
                    String url = uploadImageFromUrl(updatedUrl, imagesReqDTO.connectedId());
                    ReviewImage reviewImage = new ReviewImage(i, url, requestedMeta.type());

                    resultImages.add(reviewImage);
                }else {
                    preImage.setPath(updatedUrl);
                }

                resultImages.add(preImage);

            } else if (preImage != null) {
                deleteObject(preImage.getPath());
                deleteImages.add(preImage);
            }

        }
        reviewImageRepository.deleteAll(deleteImages);
        reviewImageRepository.saveAll(resultImages);

        return resultImages; // 혹은 저장된 엔티티 반환
    }
}
