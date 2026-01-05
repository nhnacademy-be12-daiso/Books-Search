package com.daisobook.shop.booksearch.service.book.impl;

import com.daisobook.shop.booksearch.dto.BookListData;
import com.daisobook.shop.booksearch.dto.BookUpdateData;
import com.daisobook.shop.booksearch.dto.DiscountDTO;
import com.daisobook.shop.booksearch.dto.point.PointPolicyType;
import com.daisobook.shop.booksearch.books_search.dto.projection.*;
import com.daisobook.shop.booksearch.dto.projection.*;
import com.daisobook.shop.booksearch.dto.request.AuthorReqDTO;
import com.daisobook.shop.booksearch.dto.request.ImageMetadataReqDTO;
import com.daisobook.shop.booksearch.dto.request.TagReqDTO;
import com.daisobook.shop.booksearch.dto.request.book.BookGroupReqV2DTO;
import com.daisobook.shop.booksearch.dto.request.book.BookReqV2DTO;
import com.daisobook.shop.booksearch.dto.request.order.OrderCancelRequest;
import com.daisobook.shop.booksearch.dto.request.review.ReviewReqDTO;
import com.daisobook.shop.booksearch.dto.response.SortBookListRespDTO;
import com.daisobook.shop.booksearch.dto.response.TotalDataRespDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.book.*;
import com.daisobook.shop.booksearch.dto.response.book.*;
import com.daisobook.shop.booksearch.dto.response.order.OrderBookSummeryDTO;
import com.daisobook.shop.booksearch.dto.response.order.OrderBooksInfoRespDTO;
import com.daisobook.shop.booksearch.dto.service.ImagesReqDTO;
import com.daisobook.shop.booksearch.entity.BookListType;
import com.daisobook.shop.booksearch.entity.book.Book;
import com.daisobook.shop.booksearch.entity.book.BookImage;
import com.daisobook.shop.booksearch.entity.book.Status;
import com.daisobook.shop.booksearch.entity.review.Review;
import com.daisobook.shop.booksearch.books_search.exception.custom.book.*;
import com.daisobook.shop.booksearch.exception.custom.book.*;
import com.daisobook.shop.booksearch.exception.custom.category.NotFoundCategoryId;
import com.daisobook.shop.booksearch.exception.custom.mapper.FailObjectMapper;
import com.daisobook.shop.booksearch.mapper.book.BookMapper;
import com.daisobook.shop.booksearch.mapper.image.ImageMapper;
import com.daisobook.shop.booksearch.search.component.BookSearchSyncPublisher;
import com.daisobook.shop.booksearch.service.category.CategoryV2Service;
import com.daisobook.shop.booksearch.service.image.impl.BookImageServiceImpl;
import com.daisobook.shop.booksearch.service.like.LikeService;
import com.daisobook.shop.booksearch.service.policy.DiscountPolicyService;
import com.daisobook.shop.booksearch.service.review.ReviewService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookFacade {
    private final BookCoreService bookCoreService;
    private final BookImageServiceImpl imageService;
    private final LikeService likeService;
    private final DiscountPolicyService discountPolicyService;
    private final ReviewService reviewService;
    private final CategoryV2Service categoryService;

    private final BookMapper bookMapper;
    private final ImageMapper imageMapper;
    private final BookSearchSyncPublisher bookSearchSyncPublisher;

    public BookGroupReqV2DTO parsing(String metadataJson, MultipartFile image0, MultipartFile image1,
                                     MultipartFile image2, MultipartFile image3, MultipartFile image4) throws JsonProcessingException {
        if(metadataJson == null){
            throw new RuntimeException("metadata is null");
        }

        return bookMapper.parsing(metadataJson, image0, image1, image2, image3, image4);
    }

    @Transactional
    public void registerBook(BookReqV2DTO bookReqDTO, Map<String, MultipartFile> fileMap){
        bookCoreService.validateNotExistsByIsbn(bookReqDTO.isbn());

        Book book = bookMapper.create(bookReqDTO);

        book = bookCoreService.registerBook(
                book,
                bookReqDTO.categoryId(),
                bookReqDTO.tags().stream().map(TagReqDTO::tagName).toList(),
                bookReqDTO.authorReqDTOList(),
                bookReqDTO.publisher()
        );

        ImagesReqDTO imagesReqDTO = imageMapper.createImagesReqDTO(book.getId(), bookReqDTO.imageMetadataReqDTOList());
        imageService.addBookImage(book, imagesReqDTO, fileMap);

        bookSearchSyncPublisher.publishUpsertAfterCommit(book, "BOOK_REGISTER");
    }

    @Transactional
    public void registerBooks(List<BookReqV2DTO> bookReqDTOList){
        Set<String> existsByIsbn = bookCoreService.getExistsByIsbn(bookReqDTOList.stream()
                    .map(BookReqV2DTO::isbn)
                    .toList()).stream()
                .map(BookIsbnProjection::getIsbn)
                .collect(Collectors.toSet());

        Map<String, Book> bookMap = new HashMap<>();
        Map<String, Long> categoryIdMap = new HashMap<>();
        Map<String, List<String>> tagNameListMap = new HashMap<>();
        Map<String, List<AuthorReqDTO>> authorListMap = new HashMap<>();
        Map<String, String> publisherNameMap = new HashMap<>();
        Map<String, List<ImageMetadataReqDTO>> imageListMap = new HashMap<>();

        for(BookReqV2DTO br: bookReqDTOList){
            if(existsByIsbn.contains(br.isbn())){
                continue;
            }
            Book book = bookMapper.create(br);

            bookMap.put(book.getIsbn(), book);
            categoryIdMap.put(book.getIsbn(), br.categoryId());
            tagNameListMap.put(book.getIsbn(), br.tags().stream().map(TagReqDTO::tagName).toList());
            authorListMap.put(book.getIsbn(), br.authorReqDTOList());
            publisherNameMap.put(book.getIsbn(), br.publisher());
            imageListMap.put(book.getIsbn(), br.imageMetadataReqDTOList());
        }

        bookMap = bookCoreService.registerBooks(
                bookMap,
                categoryIdMap,
                tagNameListMap,
                authorListMap,
                publisherNameMap
        );

        List<ImagesReqDTO> imagesReqDTOList = imageMapper.createImagesReqDTOList(bookMap, imageListMap);
        imageService.addBookImages(bookMap, imagesReqDTOList);

    }

    @Transactional
    public void updateBook(long bookId, BookReqV2DTO bookReqDTO, Map<String, MultipartFile> fileMap) {
        Book book = bookCoreService.getBook_Id(bookId);
        if(book == null){
            log.error("[도서 수정] 해당 도서를 찾지 못했습니다 - 도서 ID: {}", bookId);
            throw new NotFoundBookId("[도서 수정] 해당 도서를 찾지 못했습니다.");
        }
        BookUpdateData bookUpdateData = bookMapper.toBookUpdateData(bookReqDTO);

        book = bookCoreService.updateBookByData(book, bookUpdateData);

        //도서 이미지 체크
        List<ImageMetadataReqDTO> imageMetadataReqDTOs = bookReqDTO.imageMetadataReqDTOList();
        Map<Integer, String> bookImageUrl = book.getBookImages().stream()
                .collect(Collectors.toMap(BookImage::getNo, BookImage::getPath));
        Map<Integer, String> metadataReqDTOUrl = imageMetadataReqDTOs.stream()
                .collect(Collectors.toMap(ImageMetadataReqDTO::sequence, ImageMetadataReqDTO::dataUrl));

        boolean imageCheck = false;
        for(int i = 0; i < 5; i++){
            boolean pre = bookImageUrl.containsKey(i);
            boolean update = metadataReqDTOUrl.containsKey(i);

            if (imageMetadataReqDTOs.size() > i)
                if(imageMetadataReqDTOs.get(i) != null &&
                imageMetadataReqDTOs.get(i).fileKey() != null &&
                fileMap.containsKey(imageMetadataReqDTOs.get(i).fileKey())) {

                    imageCheck = true;
                    break;
                }

            if(pre && update) {
                if (!bookImageUrl.get(i).equals(metadataReqDTOUrl.get(i))) {
                    imageCheck = true;
                    break;
                }
            } else if (pre || update) {
                imageCheck = true;
                break;
            }

            bookSearchSyncPublisher.publishUpsertAfterCommit(book, "BOOK_UPDATE");
        }

        if(imageCheck){
            ImagesReqDTO imagesReqDTO = imageMapper.createImagesReqDTO(book.getId(), bookReqDTO.imageMetadataReqDTOList());
            imageService.updateBookImage(book, imagesReqDTO, fileMap);
        }
    }

    @Transactional
    public void deleteBookById(long bookId){
        Book book = bookCoreService.getBook_Id(bookId);
        if(book == null){
            log.error("[도서 삭제] 해당 도서 ID 해당 해당 도서를 찾지 못했습니다 - 도서 ID: {}", bookId);
            throw new NotFoundBookId("[도서 삭제] 해당 도서 ID 해당 도서를 찾지 못했습니다.");
        }

        book = bookCoreService.deleteBookByData(book);

        String isbn=book.getIsbn();

        imageService.deleteBookImageOfBook(book);

        bookCoreService.deleteBook(book);

        bookSearchSyncPublisher.publishDeleteAfterCommit(isbn, "BOOK_DELETE");
    }

    @Transactional
    public void deleteBookByIsbn(String isbn){
        long bookId = bookCoreService.getBookIdByIsbn(isbn);
        deleteBookById(bookId);
    }

    @Transactional(readOnly = true)
    public BookRespDTO getBookDetail(long bookId, Long userId){
        BookDetailProjection detail = bookCoreService.getBookDetail_Id(bookId);

        Integer likeCount = likeService.likeCount(detail.getId());
        Boolean likeCheck = likeService.likeCheck(detail.getId(), userId);
        Long discountPrice = null;
        try {
            discountPrice = discountPolicyService.getDiscountPrice(detail.getId(), detail.getPrice());
        } catch (JsonProcessingException e) {
            log.error("[도서 상세] 할인 정책 매핑을 실패했습니다");
            throw new FailObjectMapper(e.getMessage());
        }

        BookRespDTO bookRespDTO = null;
        try {
            bookRespDTO = bookMapper.toBookRespDTO(detail, likeCount, likeCheck, discountPrice);
        } catch (JsonProcessingException e) {
            log.error("[도서 상세] 도서 매핑을 실패했습니다");
            throw new FailObjectMapper(e.getMessage());
        }

        return bookRespDTO;
    }

    @Transactional(readOnly = true)
    public BookUpdateView getBookUpdateView(long bookId){
        BookUpdateViewProjection detail = bookCoreService.getBookUpdateView_Id(bookId);

        BookUpdateView bookUpdateView = null;
        try {
            bookUpdateView = bookMapper.toBookUpdateView(detail);
        } catch (JsonProcessingException e) {
            log.error("[도서 수정 조회] 도서 매핑을 실패했습니다");
            throw new FailObjectMapper(e.getMessage());
        }

        return bookUpdateView;
    }

    @Transactional(readOnly = true)
    public SortBookListRespDTO getBookList(Pageable page, BookListType listType, Long userId){
        if(listType == null){
            log.error("[도서 목록] book list type가 null입니다");
            throw new BookListTypeNull("[도서 목록] book list type가 null입니다");
        }

        List<Long> bookIds = null;
        if(listType.equals(BookListType.NEW_RELEASES)){
            LocalDate startDate = LocalDate.now().minusMonths(60);
            bookIds = bookCoreService.getBookIdsOfNewReleases(page, startDate, 10);
        } else if(listType.equals(BookListType.BOOK_OF_THE_WEEK)){
            bookIds = bookCoreService.getBookIdsFromBookOfTheWeek(page, 10);
        }

        if(bookIds == null){
            log.error("[도서 목록]도서 조회 실패");
            throw new NotFoundBook("[도서 목록]도서 조회 실패");
        }

        List<BookListProjection> bookListProjections = bookCoreService.getBookByIds(bookIds, false);
        Map<Long, BookListData> bookListDataMap = null;
        try {
            bookListDataMap = bookMapper.toBookListDataMap(bookListProjections);
        } catch (JsonProcessingException e) {
            log.error("[도서 목록] 도서 매핑을 실패했습니다");
            throw new FailObjectMapper(e.getMessage());
        }
        if(bookListDataMap == null){
            return null;
        }

        Set<Long> likeSetBookId = likeService.getLikeByUserIdAndBookIds(userId,
                bookListProjections.stream()
                        .map(BookListProjection::getId)
                        .toList());

        Map<Long, DiscountDTO.Request> discountDTOMap = bookMapper.toDiscountDTOMapByBookListData(bookListDataMap);
        Map<Long, DiscountDTO.Response> discountPriceMap = null;
        try {
            discountPriceMap = discountPolicyService.getDiscountPriceMap(discountDTOMap);
        } catch (JsonProcessingException e) {
            log.error("[도서 목록] 할인 정책 매핑을 실패했습니다");
            throw new FailObjectMapper(e.getMessage());
        }
        if(discountPriceMap == null){
            return null;
        }

        List<BookListRespDTO> bookRespDTOList = bookMapper.toBookRespDTOList(bookListDataMap, discountPriceMap, likeSetBookId);

        return new SortBookListRespDTO(bookRespDTOList);
    }

    @Transactional(readOnly = true)
    public OrderBooksInfoRespDTO findBooksByIdIn(List<Long> bookId){
        List<BookInfoListProjection> bookListProjections = bookCoreService.getBookInfoListByInd(bookId, false);
        if(bookListProjections == null || bookListProjections.isEmpty()){
            return null;
        }

        Map<Long, DiscountDTO.Request> discountDTOMap = bookMapper.toDiscountDTOMapByBookInfoListProjection(bookListProjections);
        Map<Long, DiscountDTO.Response> discountPriceMap = null;
        try {
            discountPriceMap = discountPolicyService.getDiscountPriceMap(discountDTOMap);
        } catch (JsonProcessingException e) {
            log.error("[주문:도서 목록] 할인 정책 매핑을 실패했습니다");
            throw new FailObjectMapper(e.getMessage());
        }
        if(discountPriceMap == null){
            return null;
        }

        return bookMapper.toOrderBookInfoRespDTOList(discountPriceMap, bookListProjections);
    }

    @Transactional(readOnly = true)
    public List<OrderBookSummeryDTO> getOrderBookList(List<Long> bookIds){
        List<BookSummeryProjection> bookSummeryProjections = bookCoreService.getBookSummeryByIds(bookIds);

        return bookMapper.toOrderBookSummeryDTOList(bookSummeryProjections);
    }

    @Transactional(readOnly = true)
    public boolean existIsbn(String isbn){
        Boolean existed = bookCoreService.existIsbn(isbn);
        if(existed == null){
            log.error("[도서 isbn 유무] 해당 도서 유무에 대한 판단 불가 - isbn:{}", isbn);
            throw new NotFoundBookISBN("[도서 isbn 유무] 해당 도서 유무에 대한 판단 불가");
        }

        return existed;
    }

    @Transactional(readOnly = true)
    public Page<BookAdminResponseDTO> findAllForAdmin(Pageable pageable){
        Page<BookAdminProjection> adminProjectionPage = bookCoreService.getBookAdminProjectionPage(pageable);

        Map<Long, DiscountDTO.Request> discountDTOMap = bookMapper.toDiscountDTOMapByBookAdminProjection(adminProjectionPage);
        Map<Long, DiscountDTO.Response> discountPriceMap = null;
        try {
            discountPriceMap = discountPolicyService.getDiscountPriceMap(discountDTOMap);
        } catch (JsonProcessingException e) {
            log.error("[도서 관리 목록] 할인 정책 매핑을 실패했습니다");
            throw new FailObjectMapper(e.getMessage());
        }
        if(discountPriceMap == null){
            return null;
        }

        Page<BookAdminResponseDTO> page = null;
        try {
            page = bookMapper.toBookAdminResopnseDTOPage(adminProjectionPage, discountPriceMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return page;
    }

    @Transactional(readOnly = true)
    public TotalDataRespDTO getTotalDate(){
        Long countBook = bookCoreService.getCountAll();
        Long countByStatus = bookCoreService.getCountByStatus(Status.SOLD_OUT);
        Long countByRelease = reviewService.getCountByRelease(7);
        Long countCategory = categoryService.getCountAll();

        return new TotalDataRespDTO(countBook, countByStatus, countByRelease, countCategory);
    }

    @Transactional
    public PointPolicyType registerReview(ReviewReqDTO reviewReqDTO, Map<String, MultipartFile> fileMap){
        Book book = bookCoreService.getBook_Id(reviewReqDTO.bookId());
        Review review = reviewService.registerReview(reviewReqDTO, fileMap, book);
        PointPolicyType type = review.getReviewImages() != null && !review.getReviewImages().isEmpty() ? PointPolicyType.REVIEW_PHOTO : PointPolicyType.REVIEW_TEXT;
        return type;
    }

    @Transactional(readOnly = true)
    public BookListByCategoryRespDTO getBookListByCategoryId(Pageable pageable, long categoryId, Long userId){
        List<Long> lowCategoryIdList = categoryService.getLowCategoryIdList(categoryId);
        if(lowCategoryIdList == null || lowCategoryIdList.isEmpty()){
            log.warn("[카테고리 도서 조회] 해당 카테고리가 존재하지 않습니다 - 카테고리Id:{}", categoryId);
            throw new NotFoundCategoryId("[카테고리 도서 조회] 해당 카테고리가 존재하지 않습니다");
        }

        Page<BookListProjection> bookList = bookCoreService.getBookByCategoryIdList(pageable, lowCategoryIdList);
        if(bookList == null || bookList.isEmpty()){
            log.info("[카테고리 도서 조회] 해당 카테고리에 속한 도서가 존재하지 않습니다 - 카테고리Id:{}", categoryId);
            return null;
        }

        Page<BookListData> bookListDataPage = null;
        try {
            bookListDataPage = bookMapper.toBookListDataPage(bookList);
        } catch (JsonProcessingException e) {
            log.error("[카테고리 도서 조회] 도서 매핑을 실패했습니다");
            throw new FailObjectMapper(e.getMessage());
        }
        if(bookListDataPage == null){
            return null;
        }

        Set<Long> likeSetBookId = likeService.getLikeByUserIdAndBookIds(userId,
                bookList.map(BookListProjection::getId)
                        .toList());

        Map<Long, DiscountDTO.Request> discountDTOMap = bookMapper.toDiscountDTOMapByBookListData(bookListDataPage);
        Map<Long, DiscountDTO.Response> discountPriceMap = null;
        try {
            discountPriceMap = discountPolicyService.getDiscountPriceMap(discountDTOMap);
        } catch (JsonProcessingException e) {
            log.error("[카테고리 도서 조회] 할인 정책 매핑을 실패했습니다");
            throw new FailObjectMapper(e.getMessage());
        }
        if(discountPriceMap == null){
            return null;
        }

        Page<BookListRespDTO> bookRespDTOList = bookMapper.toBookRespDTOPage(bookListDataPage, discountPriceMap, likeSetBookId);

        return new BookListByCategoryRespDTO(bookRespDTOList);
    }

    @Transactional
    public void orderCancel(OrderCancelRequest request){
        Book book = bookCoreService.getBook_Id(request.bookId());
        if(book == null){
            log.warn("[주문 취소] 해당 도서를 찾지 못하였습니다 - 도서ID:{}", request.bookId());
            throw new NotFoundBookId("[주문 취소] 해당 도서를 찾지 못하였습니다");
        }

        int quantity = request.quantity();
        int stock = book.getStock();
        if(quantity > 0){
            if(book.getStatus().equals(Status.SOLD_OUT)){
                book.setStatus(Status.ON_SALE);
            }
            book.setStock(stock + quantity);
        } else if(quantity < 0){
            int resultStock = stock + quantity;
            if(resultStock < 0){
                log.warn("[주문 취소] 해당 수량이 재고량 차감이 불가한 수치입니다 - 재고량:{}, 차감 수량:{}", stock, quantity);
                throw new DuplicatedBook("[주문 취소] 해당 수량이 재고량 차감이 불가한 수치입니다");
            }

            if(resultStock == 0 && book.getStatus().equals(Status.ON_SALE)){
                book.setStatus(Status.SOLD_OUT);
            }
            book.setStock(resultStock);
        } else {
            log.info("[주문 취소] 수량이 0이라서 재고 변경이 없습니다");
        }
    }
}
