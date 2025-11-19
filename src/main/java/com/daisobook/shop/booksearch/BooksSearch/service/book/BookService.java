package com.daisobook.shop.booksearch.BooksSearch.service.book;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.*;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.BookRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.Book;

import java.util.List;

public interface BookService {
    void validateExistsById(long bookId);
    void validateExistsByIsbn(String isbn);
    void validateNotExistsByIsbn(String isbn);
    void assignCategoriesToBook(Book book, List<CategoryReqDTO> categories);
    void assignTagsToBook(Book book, List<String> tags);
    void registerBook(BookReqDTO bookReqDTO);
    void registerBooks(List<BookReqDTO> bookReqDTOS);
    BookRespDTO findBookById(long bookId);
    BookRespDTO findBookByIsbn(String isbn);
    List<BookRespDTO> findBooks(String categoryName, String tagName, String author, String publisher);
    void updateBook(long bookId, BookReqDTO BookReqDTO);
    void deleteBook(DeleteBookReqDTO deleteBookReqDTO);
}
