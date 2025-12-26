//package com.daisobook.shop.booksearch.BooksSearch.controller;
//
//import com.daisobook.shop.booksearch.BooksSearch.dto.request.AuthorReqDTO;
//import com.daisobook.shop.booksearch.BooksSearch.dto.response.AuthorRespDTO;
//import com.daisobook.shop.booksearch.BooksSearch.service.author.AuthorService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RequiredArgsConstructor
//@Service
//@RequestMapping("/api/books/authors")
//public class AuthorController {
//
//    private final AuthorService authorService;
//
//    @PostMapping("/roles/batch")
//    public ResponseEntity addRoles(List<AuthorReqDTO> authorReqDTOList){
//        authorService.registerRoles(authorReqDTOList);
//        return ResponseEntity.ok().build();
//    }
//
//    @PostMapping("/roles")
//    public ResponseEntity addRole(AuthorReqDTO authorReqDTO){
//        authorService.registerRole(authorReqDTO);
//        return ResponseEntity.ok().build();
//    }
//
//    @PostMapping
//    public ResponseEntity addAuthor(AuthorReqDTO authorReqDTO){
//        authorService.registerAuthor(authorReqDTO);
//        return ResponseEntity.ok().build();
//    }
//
//    @GetMapping("/{authorId}")
//    public AuthorRespDTO getAuthor(@RequestParam("authorId") long authorId){
//        return authorService.getAuthorByAuthorId(authorId);
//    }
//
//    @GetMapping("/roles/{roleId}")
//    public AuthorRespDTO getRole(@RequestParam("roleId") long roleId){
//        return authorService.getAuthorByRoleId(roleId);
//    }
//
//    @GetMapping("/author?roleId={roleId}")
//    public List<AuthorRespDTO> getAuthorsByRoleId(@PathVariable("roleId") long roleId){
//        return authorService.getAuthorsByRoleId(roleId);
//    }
//
//    @PutMapping("/{authorId}")
//    public ResponseEntity updateAuthor(@RequestParam("authorId") long authorId,
//                                  AuthorReqDTO authorReqDTO){
//        authorService.updateAuthor(authorId, authorReqDTO);
//        return ResponseEntity.ok().build();
//    }
//
//    @PutMapping("/roles/{roleId}")
//    public ResponseEntity updateRole(@RequestParam("roleId") long roleId,
//                                       AuthorReqDTO authorReqDTO){
//        authorService.updateRole(roleId, authorReqDTO);
//        return ResponseEntity.ok().build();
//    }
//
//    @DeleteMapping("/{authorId}")
//    public ResponseEntity deleteAuthor(@RequestParam("authorId") long authorId){
//        authorService.deleteAuthor(authorId);
//        return ResponseEntity.ok().build();
//    }
//
//    @DeleteMapping("/roles/{roleId}")
//    public ResponseEntity deleteRole(@RequestParam("roleId") long roleId){
//        authorService.deleteRole(roleId);
//        return ResponseEntity.ok().build();
//    }
//}
