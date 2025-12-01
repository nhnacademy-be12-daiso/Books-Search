package com.daisobook.shop.booksearch.BooksSearch.service.book;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.*;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookGroupReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookMetadataReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.BookRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.Book;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface BookService {
    BookGroupReqDTO parsing(BookMetadataReqDTO dto) throws JsonProcessingException;
    void validateExistsById(long bookId);
    void validateExistsByIsbn(String isbn);
    void validateNotExistsByIsbn(String isbn);
    void assignCategoriesToBook(Book book, List<CategoryReqDTO> categories);
    void assignTagsToBook(Book book, List<String> tagNames);
    void assignAuthorToBook(Book book, List<AuthorReqDTO> authorReqDTOs);
    void assignImages(Book book, List<ImageMetadataReqDTO> dto, Map<String, MultipartFile> fileMap);
    void registerBook(BookReqDTO bookReqDTO, Map<String, MultipartFile> fileMap);
    void registerBooks(List<BookReqDTO> bookReqDTOSs);
    BookRespDTO findBookById(long bookId);
    BookRespDTO findBookByIsbn(String isbn);
    List<BookRespDTO> findBooks(String categoryName, String tagName, String authorName, String publisherName);
    List<BookRespDTO> getBooksByIdIn(BookIdListReqDTO bookIdListReqDTO);
    void updateBook(long bookId, BookReqDTO BookReqDTO,Map<String, MultipartFile> fileMap);
    void deleteBook(DeleteBookReqDTO deleteBookReqDTO);

    //다른 서비스에서 사용하는 메서드
    Book getBookById(long bookId);
}
