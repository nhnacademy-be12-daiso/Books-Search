package com.daisobook.shop.booksearch.BooksSearch.service.review.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.ImageMetadataReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookGroupReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookMetadataReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.review.ReviewGroupReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.review.ReviewMetadataReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.review.ReviewReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.ImageRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.ReviewRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.service.ImagesReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.BookImage;
import com.daisobook.shop.booksearch.BooksSearch.entity.Review;
import com.daisobook.shop.booksearch.BooksSearch.entity.ReviewImage;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.CannotChangedReview;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.DuplicatedReview;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.NotFoundBook;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.NotFoundReview;
import com.daisobook.shop.booksearch.BooksSearch.repository.ReviewRepository;
import com.daisobook.shop.booksearch.BooksSearch.service.book.BookService;
import com.daisobook.shop.booksearch.BooksSearch.service.image.impl.ReviewImageServiceImpl;
import com.daisobook.shop.booksearch.BooksSearch.service.review.ReviewService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageServiceImpl imageService;

    private final ObjectMapper objectMapper;

    @Override
    public ReviewGroupReqDTO parsing(ReviewMetadataReqDTO dto) throws JsonProcessingException {
        if(dto == null){
            throw new RuntimeException("null");
        }

        final int MAX_FILE_COUNT = 5;

        ReviewReqDTO metadata = objectMapper.readValue(dto.metadata(), ReviewReqDTO.class);
        Map<String, MultipartFile> files = new HashMap<>();
        Class<?> clazz = dto.getClass();

        for(int i = 0; i < MAX_FILE_COUNT; i++) {
            String key = "image%d".formatted(i);
            try {
                // DTO에서 필드를 찾아 접근 권한 설정
                Field field = clazz.getDeclaredField(key);
                field.setAccessible(true);

                // DTO 인스턴스에서 해당 필드의 값(MultipartFile) 추출
                MultipartFile file = (MultipartFile) field.get(dto);

                // 파일이 비어있지 않은 경우에만 Map에 추가 (Key는 "image0", "image1"...)
                if (file != null && !file.isEmpty()) {
                    files.put(key, file);
                }
            } catch (NoSuchFieldException e) {
                // 필드가 없으면 종료
                break;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return new ReviewGroupReqDTO(metadata, files);
    }

    @Transactional
    @Override
    public void registerReview(ReviewReqDTO reviewReqDTO, Map<String, MultipartFile> fileMap, Book book){
        if(reviewRepository.existsReviewByBook_IdAndUserIdAndOderDetailId(reviewReqDTO.bookId(), reviewReqDTO.userId(), reviewReqDTO.orderDetailId())){
            log.error("이미 존재하는 리뷰 생성 요청입니다-  bookId:{}, userId:{}, oderDetailId:{}",
                    reviewReqDTO.bookId(), reviewReqDTO.userId(), reviewReqDTO.orderDetailId());
            throw new DuplicatedReview("이미 존재하는 리뷰 생성 요청입니다");
        }

        if(book == null){
            log.error("해당 아이디의 도서를 찾지 못하였습니다 - 도서ID:{}", reviewReqDTO.bookId());
            throw new NotFoundBook("해당 아이디의 도서를 찾지 못하였습니다");
        }

        Review newReview = Review.create(reviewReqDTO, book);
        reviewRepository.save(newReview);

        ImagesReqDTO reqDTO = new ImagesReqDTO(newReview.getId(), reviewReqDTO.imageMetadataReqDTOList());
        List<ReviewImage> reviewImages = imageService.addReviewImage(reqDTO, fileMap);

        newReview.setReviewImages(reviewImages);
        reviewImages.forEach(ri -> ri.setReview(newReview));
        book.getReviews().add(newReview);
    }

    @Transactional
    @Override
    public ReviewRespDTO getReviewById(long id){
        Review review = reviewRepository.findReviewById(id);
        if(review == null){
            log.error("존재하지 않는 리뷰 아이디입니다 - reviewId:{}", id);
            throw new NotFoundReview("존재하지 않는 리뷰 아이디입니다");
        }

        return new ReviewRespDTO(review.getId(), review.getBook().getId(), review.getBook().getTitle(), review.getUserId(),
                review.getContent(), review.getRating(), review.getCreatedAt(), review.getModifiedAt(),
                review.getReviewImages().stream()
                        .map(ri -> new ImageRespDTO(ri.getNo(), ri.getPath(), ri.getImageType()))
                        .toList());
    }

    @Transactional
    @Override
    public List<ReviewRespDTO> getReviewsByUserId(long userId){
        return createReviewRespDTOList(reviewRepository.findAllByUserId(userId));
    }

    @Transactional
    @Override
    public List<ReviewRespDTO> getReviewsByBookId(long bookId){
        return createReviewRespDTOList(reviewRepository.findAllByBook_Id(bookId));
    }

    private List<ReviewRespDTO> createReviewRespDTOList(List<Review> reviewList){
        if(reviewList.isEmpty()){
            return List.of();
        }

        return reviewList.stream()
                .map(rl ->
                        new ReviewRespDTO(rl.getId(), rl.getBook().getId(), rl.getBook().getTitle(), rl.getUserId(),
                                rl.getContent(), rl.getRating(), rl.getCreatedAt(), rl.getModifiedAt(),
                                rl.getReviewImages().stream()
                                        .map(ri ->
                                                new ImageRespDTO(ri.getNo(), ri.getPath(), ri.getImageType()))
                                        .toList()))
                .toList();
    }


    @Transactional
    @Override
    public void updateReview(long reviewId, ReviewReqDTO reviewReqDTO, Map<String , MultipartFile> fileMap){
        Review review = reviewRepository.findReviewById(reviewId);
        if(review == null){
            log.error("해당하는 리뷰를 찾지 못했습니다 - reviewId:{}", reviewId);
            throw new NotFoundReview("해당하는 리뷰를 찾지 못했습니다");
        }

        if(!(review.getBook().getId() == reviewReqDTO.bookId())
                || !(review.getUserId() == reviewReqDTO.userId())
                || !(review.getOderDetailId() == reviewReqDTO.orderDetailId())){
            log.error("일치하는 리뷰 정보가 아닙니다 - reviewId:{}, bookId:{}, userId:{}, oderDetailId:{}",
                    reviewId, reviewReqDTO.bookId(), reviewReqDTO.userId(), review.getOderDetailId());
            throw new NotFoundReview("일치하는 리뷰 정보가 아닙니다");
        }

        if(!review.getContent().equals(reviewReqDTO.content())){
            review.setContent(reviewReqDTO.content());
        }
        if(review.getRating() != reviewReqDTO.rating()){
            int rating = reviewReqDTO.rating();
            if(rating < 1 || rating > 5){
                log.error("평점은 1~5 사이의 정수로만 변경이 가능합니다 - 변경 요청한 평점:{}", rating);
                throw new CannotChangedReview("평점은 1~5 사이의 정수로만 변경이 가능합니다");
            }
            review.setRating(reviewReqDTO.rating());
        }

        List<ReviewImage> reviewImages = review.getReviewImages();
        List<ImageMetadataReqDTO> imageMetadataReqDTOs = reviewReqDTO.imageMetadataReqDTOList();
        Map<Integer, String> reviewImageUrl = reviewImages.stream().collect(Collectors.toMap(ReviewImage::getNo, ReviewImage::getPath));
        Map<Integer, String> metadataReqDTOUrl = imageMetadataReqDTOs.stream().collect(Collectors.toMap(ImageMetadataReqDTO::sequence, ImageMetadataReqDTO::dataUrl));

        boolean imageChangeDetected = false;
        for(int i = 0; i < 5; i++){
            boolean pre = reviewImageUrl.containsKey(i);
            boolean update = metadataReqDTOUrl.containsKey(i);

            if (imageMetadataReqDTOs.size() > i)
                if(
                        imageMetadataReqDTOs.get(i) != null &&
                                imageMetadataReqDTOs.get(i).fileKey() != null &&
                                fileMap.containsKey(imageMetadataReqDTOs.get(i).fileKey())) {

                    imageChangeDetected = true;
                    break;
                }

            if(pre && update) {
                if (!reviewImageUrl.get(i).equals(metadataReqDTOUrl.get(i))) {
                    imageChangeDetected = true;
                    break;
                }
            } else if (pre || update) {
                imageChangeDetected = true;
                break;
            }
        }
        if(imageChangeDetected){
            ImagesReqDTO reqDTO = new ImagesReqDTO(review.getId(), imageMetadataReqDTOs);
            List<ReviewImage> reviewImageList = imageService.updateReviewImage(reqDTO, fileMap);
            review.setReviewImages(reviewImageList);
            if(reviewImageList != null) {
                reviewImageList.forEach(bi -> bi.setReview(review));
            }
        }
    }

    @Override
    public Review findReviewByUserIdAndBookIdAndOrderDetailId(long userId, long bookId, long orderDetailId) {
        return reviewRepository.findReviewByUserIdAndBook_IdAndOderDetailId(userId, bookId, orderDetailId);
    }
}
