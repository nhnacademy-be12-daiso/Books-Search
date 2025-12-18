package com.daisobook.shop.booksearch.BooksSearch.mapper.book.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.BookListData;
import com.daisobook.shop.booksearch.BooksSearch.dto.BookUpdateData;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookListProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookGroupReqV2DTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookReqV2DTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.*;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.BookListRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.BookRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.order.OrderBookInfoRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.order.OrderBooksInfoRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.mapper.author.AuthorMapper;
import com.daisobook.shop.booksearch.BooksSearch.mapper.book.BookMapper;
import com.daisobook.shop.booksearch.BooksSearch.mapper.category.CategoryMapper;
import com.daisobook.shop.booksearch.BooksSearch.mapper.image.ImageMapper;
import com.daisobook.shop.booksearch.BooksSearch.mapper.publisher.PublisherMapper;
import com.daisobook.shop.booksearch.BooksSearch.mapper.review.ReviewMapper;
import com.daisobook.shop.booksearch.BooksSearch.mapper.tag.TagMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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
                req.tagNameList(),req.isDeleted());
    }

    @Override
    public OrderBooksInfoRespDTO toOrderBookInfoRespDTOList(List<Book> bookList, Map<Long, Long> discountPriceMap){

        return new OrderBooksInfoRespDTO(bookList.stream()
                .map(b ->
                        new OrderBookInfoRespDTO(b.getId(), b.getTitle(), b.getPrice(), b.getStock(),
                                discountPriceMap.containsKey(b.getId()) ? BigDecimal.valueOf((double) discountPriceMap.get(b.getId()) / b.getPrice() * 100.0) : null,
                                discountPriceMap.getOrDefault(b.getId(), null), b.getBookImages() != null ? b.getBookImages().getFirst() != null ? b.getBookImages().getFirst().getPath() : null : null))
                .toList());
    }

    @Override
    public BookRespDTO toBookRespDTO(Book book, Integer likeCount, Boolean likeCheck, Long discountPrice) {
        List<CategoryRespDTO> categoryRespDTOS = categoryMapper.toCategoryRespDTOList(book.getBookCategories());
        List<TagRespDTO> tagRespDTOS = tagMapper.toTagRespDTOList(book.getBookTags());
        List<ImageRespDTO> imageRespDTOS = imageMapper.toImageRespDTOList(book.getBookImages());
        List<ReviewRespDTO> reviews = reviewMapper.toReviewRespDTOList(book.getReviews());
        List<AuthorRespDTO> authorRespDTOS = authorMapper.toAuthorRespDTOList(book.getBookAuthors());

        BigDecimal i = discountPrice != null && book.getPrice() != null ? BigDecimal.valueOf((1.0 - (double) discountPrice / book.getPrice()) * 100.0): null;

        return new BookRespDTO(book.getId(), book.getIsbn(), book.getTitle(), book.getIndex(), book.getDescription(),
                authorRespDTOS, book.getPublisher().getName(), book.getPublicationDate(), book.getPrice(),
                Objects.requireNonNull(i).setScale(2, RoundingMode.DOWN), discountPrice, book.isPackaging(),
                book.getStock(), book.getStatus(), imageRespDTOS, book.getVolumeNo(), categoryRespDTOS,
                tagRespDTOS, likeCount, likeCheck, reviews);
    }

    @Override
    public List<BookListRespDTO> toBookRespDTOList(Map<Long, BookListData> bookListDataMap, Set<Long> likeSetBookId) {
        return bookListDataMap.values().stream()
                .map(bl ->
                        new BookListRespDTO(bl.getId(), bl.getIsbn(), bl.getTitle(), bl.getAuthorList(), bl.getPublisher().name(),
                                bl.getPublicationDate(), bl.getPrice(), bl.getDiscountPercentage(), bl.getDiscountPrice(), bl.getStatus(),
                                bl.getImageList(), bl.getCategoryList(), bl.getTagList(), bl.getVolumeNo(), bl.getIsPackaging(),
                                likeSetBookId != null ? likeSetBookId.contains(bl.getId()) : null))
                .toList();
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
                        new BookListData(bl.getId(), bl.getIsbn(), bl.getTitle(),
                                authorKeySet.contains(bl.getId()) ? authorRespDTOMap.get(bl.getId()) : null,
                                publishserKeySet.contains(bl.getId()) ? publisherRespDTOMap.get(bl.getId()) : null,
                                bl.getPublicationDate(), bl.getPrice(), null,
                                null, bl.getStatus(),
                                imageKeySet.contains(bl.getId()) ? imageRespDTOMap.get(bl.getId()) : null,
                                categoryKeySet.contains(bl.getId()) ? categoryRespDTOMap.get(bl.getId()) : null,
                                tagKeySet.contains(bl.getId()) ? tagRespDTOMap.get(bl.getId()) : null,
                                bl.getVolumeNo(), bl.getIsPackaging())));
    }
}
