package com.daisobook.shop.booksearch.BooksSearch.service.book;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.AddBookReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.AddCategoryReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.AddTagReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.BookRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.Book;

import java.util.List;

public interface BookService {
    void validateExistsById(long bookId);
    void validateExistsByIsbn(String isbn);
    void validateNotExistsByIsbn(String isbn);
    void assignCategoriesToBook(Book book, List<AddCategoryReqDTO> categories);
    void assignTagsToBook(Book book, List<AddTagReqDTO> tags);
    void registerBook(AddBookReqDTO addBookReqDTO);
    void registerBooks(List<AddBookReqDTO> addBookReqDTOS);
    BookRespDTO findBookById(long id);
    BookRespDTO findBookByIsbn(String isbn);
    List<BookRespDTO> findBooksByCategory(String categoryNam);
    List<BookRespDTO> findBooksByTag(String tagNam);
    List<BookRespDTO> findBooksByAuthor(String author);
    List<BookRespDTO> findBooksByPublisher(String publisher);
}
