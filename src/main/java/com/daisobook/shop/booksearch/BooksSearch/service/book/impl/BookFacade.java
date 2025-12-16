package com.daisobook.shop.booksearch.BooksSearch.service.book.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.BookUpdateData;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookIsbnProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.AuthorReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.ImageMetadataReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookGroupReqV2DTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookReqV2DTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.service.ImagesReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.BookImage;
import com.daisobook.shop.booksearch.BooksSearch.mapper.book.BookMapper;
import com.daisobook.shop.booksearch.BooksSearch.mapper.image.ImageMapper;
import com.daisobook.shop.booksearch.BooksSearch.service.image.impl.BookImageServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    private final BookMapper bookMapper;
    private final ImageMapper imageMapper;

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
                bookReqDTO.tagNameList(),
                bookReqDTO.authorReqDTOList(),
                bookReqDTO.publisher()
        );

        ImagesReqDTO imagesReqDTO = imageMapper.createImagesReqDTO(book.getId(), bookReqDTO.imageMetadataReqDTOList());
        imageService.addBookImage(book, imagesReqDTO, fileMap);
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
            tagNameListMap.put(book.getIsbn(), br.tagNameList());
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
        BookUpdateData bookUpdateData = bookMapper.toBookUpdateData(bookReqDTO);

        bookCoreService.updateBook(book, bookUpdateData);

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
                if(
                        imageMetadataReqDTOs.get(i) != null &&
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
        }

        if(imageCheck){
            ImagesReqDTO imagesReqDTO = imageMapper.createImagesReqDTO(book.getId(), bookReqDTO.imageMetadataReqDTOList());
            imageService.updateBookImage(book, imagesReqDTO, fileMap);
        }
    }

}
