package com.daisobook.shop.booksearch.books_search.mapper.book.impl;

import com.daisobook.shop.booksearch.books_search.dto.DiscountDTO;
import com.daisobook.shop.booksearch.books_search.dto.BookListData;
import com.daisobook.shop.booksearch.books_search.dto.BookUpdateData;
import com.daisobook.shop.booksearch.books_search.dto.projection.*;
import com.daisobook.shop.booksearch.books_search.dto.request.TagReqDTO;
import com.daisobook.shop.booksearch.books_search.dto.request.book.BookGroupReqV2DTO;
import com.daisobook.shop.booksearch.books_search.dto.request.book.BookReqV2DTO;
import com.daisobook.shop.booksearch.books_search.dto.response.*;
import com.daisobook.shop.booksearch.books_search.dto.response.book.BookAdminResponseDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.book.BookListRespDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.book.BookRespDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.book.BookUpdateView;
import com.daisobook.shop.booksearch.books_search.dto.response.category.CategoryRespDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.order.OrderBookInfoRespDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.order.OrderBookSummeryDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.order.OrderBooksInfoRespDTO;
import com.daisobook.shop.booksearch.books_search.entity.book.Book;
import com.daisobook.shop.booksearch.books_search.mapper.author.AuthorMapper;
import com.daisobook.shop.booksearch.books_search.mapper.book.BookMapper;
import com.daisobook.shop.booksearch.books_search.mapper.category.CategoryMapper;
import com.daisobook.shop.booksearch.books_search.mapper.image.ImageMapper;
import com.daisobook.shop.booksearch.books_search.mapper.publisher.PublisherMapper;
import com.daisobook.shop.booksearch.books_search.mapper.review.ReviewMapper;
import com.daisobook.shop.booksearch.books_search.mapper.tag.TagMapper;
import com.daisobook.shop.booksearch.books_search.util.MarkdownUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class BookMapperImpl implements BookMapper {
    private final ObjectMapper objectMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final ReviewMapper reviewMapper;
    private final AuthorMapper authorMapper;
    private final ImageMapper imageMapper;
    private final PublisherMapper publisherMapper;

    @Override
    public BookGroupReqV2DTO parsing(String metadataJson, MultipartFile image0, MultipartFile image1,
                                     MultipartFile image2, MultipartFile image3, MultipartFile image4) throws JsonProcessingException {

        BookReqV2DTO metadata = objectMapper.readValue(metadataJson, BookReqV2DTO.class);
        Map<String, MultipartFile> files = new HashMap<>();

        if(image0 != null){
            files.put(image0.getName(), image0);
        }
        if(image1 != null){
            files.put(image1.getName(), image1);
        }
        if(image2 != null){
            files.put(image2.getName(), image2);
        }
        if(image3 != null){
            files.put(image3.getName(), image1);
        }
        if(image4 != null){
            files.put(image4.getName(), image2);
        }

        return new BookGroupReqV2DTO(metadata, files);
    }

    @Override
    public Book create(BookReqV2DTO req) {
        Book book = new Book(req.isbn(), req.title(), req.index(), req.description(), req.publicationDate(),
                req.price(), req.isPackaging(), req.stock(), req.status(), req.volumeNo());

        if(req.isDeleted()){
            book.setDeleted(true);
        }

        return book;
    }

    @Override
    public BookUpdateData toBookUpdateData(BookReqV2DTO req){
        return new BookUpdateData(req.title(), req.index(), req.description(), req.authorReqDTOList(), req.publisher(),
                req.publicationDate(), req.price(), req.isPackaging(), req.stock(), req.status(), req.volumeNo(), req.categoryId(),
                req.tags().stream().map(TagReqDTO::tagName).toList(),req.isDeleted());
    }

    @Override
    public OrderBooksInfoRespDTO toOrderBookInfoRespDTOList(List<Book> bookList, Map<Long, Long> discountPriceMap){

        return new OrderBooksInfoRespDTO(bookList.stream()
                .map(b ->
                        new OrderBookInfoRespDTO(b.getId(), b.getTitle(), b.getPrice(), b.getStock(), b.getStatus(),
                                discountPriceMap.containsKey(b.getId()) ? BigDecimal.valueOf((double) discountPriceMap.get(b.getId()) / b.getPrice() * 100.0) : null,
                                discountPriceMap.getOrDefault(b.getId(), null),
                                b.getBookImages() != null && !b.getBookImages().isEmpty() && b.getBookImages().getFirst() != null
                                        && b.getBookImages().getFirst().getPath() != null
                                        ? b.getBookImages().getFirst().getPath() : null,
                                b.getVolumeNo(), b.isPackaging()))
                .toList());
    }

    @Override
    public OrderBooksInfoRespDTO toOrderBookInfoRespDTOList(Map<Long, DiscountDTO.Response> discountDTOMap, List<BookInfoListProjection> bookInfoListProjections) {
        return new OrderBooksInfoRespDTO(bookInfoListProjections.stream()
                .map(bi ->
                        new OrderBookInfoRespDTO(bi.getBookId(), bi.getTitle(), bi.getPrice(), bi.getStock(), bi.getStatus(),
                                discountDTOMap.containsKey(bi.getBookId()) ? discountDTOMap.get(bi.getBookId()).discountPercentage() : null,
                                discountDTOMap.containsKey(bi.getBookId()) ? discountDTOMap.get(bi.getBookId()).discountPrice() : null,
                                bi.getCoverImage(), bi.getVolumeNo(), bi.getIsPackaging()))
                .toList());
    }

    @Override
    public BookRespDTO toBookRespDTO(BookDetailProjection bookDetail, Integer likeCount, Boolean likeCheck, Long discountPrice) throws JsonProcessingException {
        List<CategoryRespDTO> categoryRespDTOS = categoryMapper.toCategoryRespDTOList(bookDetail.getCategories());
        List<TagRespDTO> tagRespDTOS = tagMapper.toTagRespDTOList(bookDetail.getTags());
        List<ImageRespDTO> imageRespDTOS = imageMapper.toImageRespDTOList(bookDetail.getImages());
        List<ReviewRespDTO> reviews = reviewMapper.toReviewRespDTOList(bookDetail.getReviews());
        List<AuthorRespDTO> authorRespDTOS = authorMapper.toAuthorRespDTOList(bookDetail.getAuthors());
        PublisherRespDTO publisherRespDTO = publisherMapper.toPublisherRespDTO(bookDetail.getPublisher());

        BigDecimal i = discountPrice != null && bookDetail.getPrice() != null ? BigDecimal.valueOf((1.0 - (double) discountPrice / bookDetail.getPrice()) * 100.0): null;

        return new BookRespDTO(bookDetail.getId(), bookDetail.getIsbn(), bookDetail.getTitle(), bookDetail.getIndex(), bookDetail.getDescription(),
                authorRespDTOS, publisherRespDTO.name(), bookDetail.getPublicationDate(), bookDetail.getPrice(),
                Objects.requireNonNull(i).setScale(2, RoundingMode.DOWN), discountPrice, bookDetail.getIsPackaging(),
                bookDetail.getStock(), bookDetail.getStatus(), imageRespDTOS, bookDetail.getVolumeNo(), categoryRespDTOS,
                tagRespDTOS, likeCount, likeCheck, reviews, bookDetail.getIsDeleted());
    }

    @Override
    public List<BookListRespDTO> toBookRespDTOList(Map<Long, BookListData> bookListDataMap, Map<Long, DiscountDTO.Response> discountPriceMap, Set<Long> likeSetBookId) {
        return bookListDataMap.values().stream()
                .map(bl ->
                        new BookListRespDTO(bl.getId(), bl.getIsbn(), bl.getTitle(), MarkdownUtils.extractPlainText(bl.getDescription()), bl.getAuthorList(), bl.getPublisher().name(),
                                bl.getPublicationDate(), discountPriceMap.containsKey(bl.getId()) ? discountPriceMap.get(bl.getId()).price() : null,
                                discountPriceMap.containsKey(bl.getId()) ? discountPriceMap.get(bl.getId()).discountPercentage() : null,
                                discountPriceMap.containsKey(bl.getId()) ? discountPriceMap.get(bl.getId()).discountPrice() : null, bl.getStatus(),
                                bl.getImageList(), bl.getCategoryList(), bl.getTagList(), bl.getVolumeNo(), bl.getIsPackaging(),
                                likeSetBookId != null ? likeSetBookId.contains(bl.getId()) : null, bl.isDeleted()))
                .toList();
    }

    @Override
    public Page<BookListRespDTO> toBookRespDTOPage(Page<BookListData> bookListDataPage, Map<Long, DiscountDTO.Response> discountPriceMap, Set<Long> likeSetBookId) {
        return bookListDataPage
                .map(bl ->
                        new BookListRespDTO(bl.getId(), bl.getIsbn(), bl.getTitle(), MarkdownUtils.extractPlainText(bl.getDescription()), bl.getAuthorList(), bl.getPublisher().name(),
                                bl.getPublicationDate(), discountPriceMap.containsKey(bl.getId()) ? discountPriceMap.get(bl.getId()).price() : null,
                                discountPriceMap.containsKey(bl.getId()) ? discountPriceMap.get(bl.getId()).discountPercentage() : null,
                                discountPriceMap.containsKey(bl.getId()) ? discountPriceMap.get(bl.getId()).discountPrice() : null, bl.getStatus(),
                                bl.getImageList(), bl.getCategoryList(), bl.getTagList(), bl.getVolumeNo(), bl.getIsPackaging(),
                                likeSetBookId != null ? likeSetBookId.contains(bl.getId()) : null, bl.isDeleted()));
    }

    @Override
    public Map<Long, BookListData> toBookListDataMap(List<BookListProjection> bookListProjectionList) throws JsonProcessingException {
        Map<Long, List<AuthorRespDTO>> authorRespDTOMap = authorMapper.toAuthorRespDTOMap(bookListProjectionList.stream()
                .filter(bl -> bl.getAuthors() != null)
                .collect(Collectors.toMap(BookListProjection::getId, BookListProjection::getAuthors)));

        Set<Long> authorKeySet = authorRespDTOMap.keySet();

        Map<Long, PublisherRespDTO> publisherRespDTOMap = publisherMapper.toPublisherRespDTOMap(bookListProjectionList.stream()
                .filter(bl -> bl.getPublisher() != null)
                .collect(Collectors.toMap(BookListProjection::getId, BookListProjection::getPublisher)));

        Set<Long> publishserKeySet = publisherRespDTOMap.keySet();

        Map<Long, List<ImageRespDTO>> imageRespDTOMap = imageMapper.toIageRespDTOMap(bookListProjectionList.stream()
                .filter(bl -> bl.getImages() != null)
                .collect(Collectors.toMap(BookListProjection::getId, BookListProjection::getImages)));

        Set<Long> imageKeySet = imageRespDTOMap.keySet();

        Map<Long, List<CategoryRespDTO>> categoryRespDTOMap = categoryMapper.toCategoryRespDTOMap(bookListProjectionList.stream()
                .filter(bl -> bl.getCategories() != null)
                .collect(Collectors.toMap(BookListProjection::getId, BookListProjection::getCategories)));

        Set<Long> categoryKeySet = categoryRespDTOMap.keySet();

        Map<Long, List<TagRespDTO>> tagRespDTOMap = tagMapper.toTagRespDTOMap(bookListProjectionList.stream()
                .filter(bl -> bl.getTags() != null)
                .collect(Collectors.toMap(BookListProjection::getId, BookListProjection::getTags)));

        Set<Long> tagKeySet = tagRespDTOMap.keySet();

        return bookListProjectionList.stream()
                .collect(Collectors.toMap(BookListProjection::getId, bl ->
                        new BookListData(bl.getId(), bl.getIsbn(), bl.getTitle(), bl.getDescription(),
                                authorKeySet.contains(bl.getId()) ? authorRespDTOMap.get(bl.getId()) : null,
                                publishserKeySet.contains(bl.getId()) ? publisherRespDTOMap.get(bl.getId()) : null,
                                bl.getPublicationDate(), bl.getPrice(), null,
                                null, bl.getStatus(),
                                imageKeySet.contains(bl.getId()) ? imageRespDTOMap.get(bl.getId()) : null,
                                categoryKeySet.contains(bl.getId()) ? categoryRespDTOMap.get(bl.getId()) : null,
                                tagKeySet.contains(bl.getId()) ? tagRespDTOMap.get(bl.getId()) : null,
                                bl.getVolumeNo(), bl.getIsPackaging(), bl.getIsDeleted())));
    }

    @Override
    public Page<BookListData> toBookListDataPage(Page<BookListProjection> bookListProjectionPage) throws JsonProcessingException {
        Map<Long, List<AuthorRespDTO>> authorRespDTOMap = authorMapper.toAuthorRespDTOMap(bookListProjectionPage.stream()
                .filter(bl -> bl.getAuthors() != null)
                .collect(Collectors.toMap(BookListProjection::getId, BookListProjection::getAuthors)));

        Set<Long> authorKeySet = authorRespDTOMap.keySet();

        Map<Long, PublisherRespDTO> publisherRespDTOMap = publisherMapper.toPublisherRespDTOMap(bookListProjectionPage.stream()
                .filter(bl -> bl.getPublisher() != null)
                .collect(Collectors.toMap(BookListProjection::getId, BookListProjection::getPublisher)));

        Set<Long> publishserKeySet = publisherRespDTOMap.keySet();

        Map<Long, List<ImageRespDTO>> imageRespDTOMap = imageMapper.toIageRespDTOMap(bookListProjectionPage.stream()
                .filter(bl -> bl.getImages() != null)
                .collect(Collectors.toMap(BookListProjection::getId, BookListProjection::getImages)));

        Set<Long> imageKeySet = imageRespDTOMap.keySet();

        Map<Long, List<CategoryRespDTO>> categoryRespDTOMap = categoryMapper.toCategoryRespDTOMap(bookListProjectionPage.stream()
                .filter(bl -> bl.getCategories() != null)
                .collect(Collectors.toMap(BookListProjection::getId, BookListProjection::getCategories)));

        Set<Long> categoryKeySet = categoryRespDTOMap.keySet();

        Map<Long, List<TagRespDTO>> tagRespDTOMap = tagMapper.toTagRespDTOMap(bookListProjectionPage.stream()
                .filter(bl -> bl.getTags() != null)
                .collect(Collectors.toMap(BookListProjection::getId, BookListProjection::getTags)));

        Set<Long> tagKeySet = tagRespDTOMap.keySet();

        return bookListProjectionPage
                .map(bl ->
                        new BookListData(bl.getId(), bl.getIsbn(), bl.getTitle(), bl.getDescription(),
                                authorKeySet.contains(bl.getId()) ? authorRespDTOMap.get(bl.getId()) : null,
                                publishserKeySet.contains(bl.getId()) ? publisherRespDTOMap.get(bl.getId()) : null,
                                bl.getPublicationDate(), bl.getPrice(), null,
                                null, bl.getStatus(),
                                imageKeySet.contains(bl.getId()) ? imageRespDTOMap.get(bl.getId()) : null,
                                categoryKeySet.contains(bl.getId()) ? categoryRespDTOMap.get(bl.getId()) : null,
                                tagKeySet.contains(bl.getId()) ? tagRespDTOMap.get(bl.getId()) : null,
                                bl.getVolumeNo(), bl.getIsPackaging(), bl.getIsDeleted()));
    }

    @Override
    public Map<Long, DiscountDTO.Request> toDiscountDTOMapByBookListData(Map<Long, BookListData> bookListDataMap) {
        return bookListDataMap.values().stream()
                .map(bl -> new DiscountDTO.Request(bl.getId(), bl.getPrice()))
                .collect(Collectors.toMap(DiscountDTO.Request::bookId, request -> request));
    }

    @Override
    public Map<Long, DiscountDTO.Request> toDiscountDTOMapByBookListData(Page<BookListData> bookListDataPage) {
        return bookListDataPage.stream()
                .map(bl -> new DiscountDTO.Request(bl.getId(), bl.getPrice()))
                .collect(Collectors.toMap(DiscountDTO.Request::bookId, request -> request));
    }

    @Override
    public Map<Long, DiscountDTO.Request> toDiscountDTOMapByBookInfoListProjection(List<BookInfoListProjection> bookInfoListDataMap) {
        return bookInfoListDataMap.stream()
                .map(bi -> new DiscountDTO.Request(bi.getBookId(), bi.getPrice()))
                .collect(Collectors.toMap(DiscountDTO.Request::bookId, request -> request));
    }

    @Override
    public Map<Long, DiscountDTO.Request> toDiscountDTOMapByBookAdminProjection(Page<BookAdminProjection> bookAdminProjectionPage) {
        return bookAdminProjectionPage.stream()
                .map(ba -> new DiscountDTO.Request(ba.getBookId(), ba.getPrice()))
                .collect(Collectors.toMap(DiscountDTO.Request::bookId, request -> request));
    }

    @Override
    public List<OrderBookSummeryDTO> toOrderBookSummeryDTOList(List<BookSummeryProjection> bookSummeryProjections) {
        return bookSummeryProjections.stream()
                .map(bp ->
                        new OrderBookSummeryDTO(bp.getBookId(), bp.getTitle(), bp.getPrice()))
                .toList();
    }

    @Override
    public BookUpdateView toBookUpdateView(BookUpdateViewProjection detail) throws JsonProcessingException {
        List<CategoryRespDTO> categoryRespDTOS = categoryMapper.toCategoryRespDTOList(detail.getCategories());
        List<TagRespDTO> tagRespDTOS = tagMapper.toTagRespDTOList(detail.getTags());
        List<ImageRespDTO> imageRespDTOS = imageMapper.toImageRespDTOList(detail.getImages());
        List<AuthorRespDTO> authorRespDTOS = authorMapper.toAuthorRespDTOList(detail.getAuthors());
        PublisherRespDTO publisherRespDTO = publisherMapper.toPublisherRespDTO(detail.getPublisher());

        return new BookUpdateView(detail.getId(), detail.getIsbn(), detail.getTitle(), detail.getIndex(),
                detail.getDescription(), authorRespDTOS, publisherRespDTO.name(), detail.getPublicationDate(),
                detail.getPrice(), detail.getIsPackaging(), detail.getStock(), detail.getStatus(), imageRespDTOS,
                detail.getVolumeNo(), categoryRespDTOS, tagRespDTOS, detail.getIsDeleted());
    }

    @Override
    public Page<BookAdminResponseDTO> toBookAdminResopnseDTOPage(Page<BookAdminProjection> adminProjectionPage, Map<Long, DiscountDTO.Response> discountPriceMap) throws JsonProcessingException {
        Map<Long, List<ImageRespDTO>> imageRespDTOMap = imageMapper.toIageRespDTOMap(adminProjectionPage.stream()
                .filter(ap -> ap.getImages() != null)
                .collect(Collectors.toMap(BookAdminProjection::getBookId, BookAdminProjection::getImages)));

        return adminProjectionPage
                .map(ap ->
                        new BookAdminResponseDTO(ap.getBookId(), ap.getIsbn(), ap.getTitle(),
                                imageRespDTOMap.getOrDefault(ap.getBookId(), null), ap.getPrice(),
                                discountPriceMap.containsKey(ap.getBookId()) ? discountPriceMap.get(ap.getBookId()).discountPercentage() : null,
                                discountPriceMap.containsKey(ap.getBookId()) ? discountPriceMap.get(ap.getBookId()).discountPrice() : null,
                                ap.getStock(), ap.getStatus(), ap.getPublicationDate(), ap.getPublisher(), ap.getIsDeleted()));
    }
}
