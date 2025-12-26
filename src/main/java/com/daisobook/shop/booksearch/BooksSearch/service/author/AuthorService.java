//package com.daisobook.shop.booksearch.BooksSearch.service.author;
//
//import com.daisobook.shop.booksearch.BooksSearch.dto.request.AuthorReqDTO;
//import com.daisobook.shop.booksearch.BooksSearch.dto.response.AuthorRespDTO;
//import com.daisobook.shop.booksearch.BooksSearch.entity.author.Author;
//import com.daisobook.shop.booksearch.BooksSearch.entity.author.Role;
//
//import java.util.List;
//
//public interface AuthorService {
//    void registerAuthor(AuthorReqDTO authorReqDTO);
//    void registerRole(AuthorReqDTO authorReqDTO);
//    void registerRoles(List<AuthorReqDTO> authorReqDTOList);
//    void updateAuthor(long authorId, AuthorReqDTO authorReqDTO);
//    void updateRole(long roleId, AuthorReqDTO authorReqDTO);
//    AuthorRespDTO getAuthorByAuthorId(long authorId);
//    AuthorRespDTO getAuthorByRoleId(long roleId);
//    List<AuthorRespDTO> getAuthorsByRoleId(long roleId);
//    void deleteAuthor(long authorId);
//    void deleteRole(long roleId);
//    Author findAuthorByName(String name);
//    Role findRoleByName(String name);
//    List<Author> findAuthorsByNameIn(List<String> authorNames);
//    List<Role> findRolesByNameIn(List<String> roleNames);
//}
