package com.daisobook.shop.booksearch.books_search.service.author;

import com.daisobook.shop.booksearch.books_search.dto.request.AuthorReqDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.RoleNameListRespDTO;
import com.daisobook.shop.booksearch.books_search.entity.book.Book;

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
