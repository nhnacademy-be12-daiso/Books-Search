package com.daisobook.shop.booksearch.BooksSearch.service.book;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.BookReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.CategoryReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.DeleteBookReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.BookRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.Book;

import java.util.List;

public interface BookService {
    void validateExistsById(long bookId);
    void validateExistsByIsbn(String isbn);
    void validateNotExistsByIsbn(String isbn);
    void assignCategoriesToBook(Book book, List<CategoryReqDTO> categories);
    void assignTagsToBook(Book book, List<String> tagNames);
    void registerBook(BookReqDTO bookReqDTO);
    void registerBooks(List<BookReqDTO> bookReqDTOS);
    BookRespDTO findBookById(long bookId);
    BookRespDTO findBookByIsbn(String isbn);
    List<BookRespDTO> findBooks(String categoryName, String tagName, String author, String publisher);
    void updateBook(long bookId, BookReqDTO BookReqDTO);
    void deleteBook(DeleteBookReqDTO deleteBookReqDTO);

    //다른 서비스에서 사용하는 메서드
    List<Book> getBooksByUser(List<Long> bookIds);
    Book getBookById(long bookId);
}
