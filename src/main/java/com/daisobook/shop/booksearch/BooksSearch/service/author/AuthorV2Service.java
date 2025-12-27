package com.daisobook.shop.booksearch.BooksSearch.service.author;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.AuthorReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.RoleNameListRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;

import java.util.List;
import java.util.Map;

public interface AuthorV2Service {
    //BookCoreService 에서 사용하는 메서드
    void assignAuthorsToBook(Book book, List<AuthorReqDTO> authorReqDTOs);
    void assignAuthorsToBooks(Map<String, Book> bookMap, Map<String, List<AuthorReqDTO>> authorListMap);
    void updateAuthorOfBook(Book book, List<AuthorReqDTO> authorReqDTOs);
    void deleteAuthorOfBook(Book book);
    RoleNameListRespDTO getRoleNameList();
}
