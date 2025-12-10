package com.daisobook.shop.booksearch.BooksSearch.service.image.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.service.ImageDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.ImageMetadataReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.service.ImagesReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.BookImage;
import com.daisobook.shop.booksearch.BooksSearch.entity.ImageType;
import com.daisobook.shop.booksearch.BooksSearch.repository.book.BookImageRepository;
import com.daisobook.shop.booksearch.BooksSearch.service.image.ImageTemplateServiceImpl;
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
public class BookImageServiceImpl extends ImageTemplateServiceImpl {

    @Value("${minio.bucketName.book}")
    private String bucketName;

    private final int MAX_SIZE = 5;

    private final BookImageRepository bookImageRepository;

    @Override
    public String getBucketName() {
        return bucketName;
    }

    public BookImageServiceImpl(MinioClient minioClient, WebClient.Builder webClientBuilder,
                                BookImageRepository bookImageRepository) {
        super(minioClient, webClientBuilder);
        this.bookImageRepository = bookImageRepository;
    }

    private List<ImageMetadataReqDTO> checkImagesCount(ImagesReqDTO imagesReqDTO){
        List<ImageMetadataReqDTO> imageDTOList = imagesReqDTO.imageMetadata();
        if(imageDTOList.size() > MAX_SIZE){
            log.error("최대 5개까지만 저장이 가능합니다 - 요청한 이미지 수:{}", imageDTOList.size());
            throw new RuntimeException("최대 5개까지만 저장이 가능합니다");
        }
        return imageDTOList;
    }

    public List<BookImage> addBookImage(ImagesReqDTO imagesReqDTO, Map<String, MultipartFile> fileMap){
        List<BookImage> bookImages = executeAdd(imagesReqDTO, fileMap);

        bookImageRepository.saveAll(bookImages);
        return bookImages;
    }

    public List<List<BookImage>> addBookImages(List<ImagesReqDTO> imagesReqDTOs){
        List<List<BookImage>> bookImagesList = new ArrayList<>();
        for(ImagesReqDTO i: imagesReqDTOs){
            List<BookImage> bookImages = executeAdd(i, null);
            bookImagesList.add(bookImages);
        }
        List<BookImage> allBookImages = bookImagesList.stream()
                .flatMap(List::stream) // List<BookImage>의 Stream을 하나의 Stream<BookImage>으로 합침
                .collect(Collectors.toList());

        bookImageRepository.saveAll(allBookImages);
        return bookImagesList;
    }

    private List<BookImage> executeAdd(ImagesReqDTO imagesReqDTO, Map<String, MultipartFile> fileMap){
        List<ImageMetadataReqDTO> imageDTOList = checkImagesCount(imagesReqDTO);

        Map<Integer, String> urlMap = createdProcess(imagesReqDTO, fileMap);

        List<BookImage> bookImages = new ArrayList<>();
        for(Map.Entry<Integer, String> entry : urlMap.entrySet()){
            int sequence = entry.getKey();
            String url = entry.getValue();

            if(sequence >= 0 && sequence < MAX_SIZE) {
                ImageType type = getLegalImageType(sequence, imageDTOList);
                bookImages.add(new BookImage(sequence, url, type));
            }
        }

        return bookImages;
    }

    private ImageType getLegalImageType(int sequence, List<ImageMetadataReqDTO> imageDTOList){
        return imageDTOList.stream()
                .filter(i -> i.sequence() == sequence)
                .map(ImageMetadataReqDTO::type)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("이미지(Sequence: " + sequence + ")의 타입을를 찾을 수 없습니다."));
    }

    public List<BookImage> updateBookImage(ImagesReqDTO imagesReqDTO, Map<String, MultipartFile> fileMap){
        checkImagesCount(imagesReqDTO);

        List<BookImage> existingImages = bookImageRepository.findBookImagesByBook_Id(imagesReqDTO.connectedId());
        Map<Integer, BookImage> existingImageMap = existingImages.stream()
                .collect(Collectors.toMap(BookImage::getNo, bi -> bi));

        List<ImageDTO> imageDTOs = existingImages.stream()
                .map(bi -> new ImageDTO(bi.getId(), bi.getBook().getId(), bi.getNo(), bi.getPath(), bi.getImageType()))
                .toList();

        Map<Integer, String> updatedUrlMap = updatedProcess(imagesReqDTO, fileMap, imageDTOs);

        Map<Integer, ImageMetadataReqDTO> requestedMetaMap = imagesReqDTO.imageMetadata().stream()
                .collect(Collectors.toMap(ImageMetadataReqDTO::sequence, dto -> dto));

        List<BookImage> resultImages = new ArrayList<>();
        List<BookImage> deleteImages = new ArrayList<>();
        for(int i = 0; i < MAX_SIZE; i++){
            BookImage preImage = existingImageMap.get(i);
            String updatedUrl = updatedUrlMap.get(i);
            ImageMetadataReqDTO requestedMeta = requestedMetaMap.get(i);

            if(updatedUrl != null) {
                if(preImage == null) {
                    String url = uploadImageFromUrl(updatedUrl, imagesReqDTO.connectedId());
                    BookImage bookImage = new BookImage(i, url, requestedMeta.type());

                    resultImages.add(bookImage);
                }else {
                    preImage.setPath(updatedUrl);
                }

                resultImages.add(preImage);

            } else if (preImage != null) {
                deleteObject(preImage.getPath());
                deleteImages.add(preImage);
            }

        }
        bookImageRepository.deleteAll(deleteImages);
        bookImageRepository.saveAll(resultImages);

        return resultImages; // 혹은 저장된 엔티티 반환
    }
}
