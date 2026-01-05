package com.daisobook.shop.booksearch.service.author.impl;

import com.daisobook.shop.booksearch.dto.projection.RoleNameProjection;
import com.daisobook.shop.booksearch.dto.request.AuthorReqDTO;
import com.daisobook.shop.booksearch.dto.response.RoleNameListRespDTO;
import com.daisobook.shop.booksearch.entity.author.Author;
import com.daisobook.shop.booksearch.entity.author.BookAuthor;
import com.daisobook.shop.booksearch.entity.author.Role;
import com.daisobook.shop.booksearch.entity.book.Book;
import com.daisobook.shop.booksearch.repository.author.AuthorRepository;
import com.daisobook.shop.booksearch.repository.author.BookAuthorRepository;
import com.daisobook.shop.booksearch.repository.author.RoleRepository;
import com.daisobook.shop.booksearch.service.author.AuthorV2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthorV2ServiceImpl implements AuthorV2Service {
    private final AuthorRepository authorRepository;
    private final RoleRepository roleRepository;
    private final BookAuthorRepository bookAuthorRepository;

    @Override
    @Transactional
    public void assignAuthorsToBook(Book book, List<AuthorReqDTO> authorReqDTOs) {
        if(authorReqDTOs == null || authorReqDTOs.isEmpty()){
            log.warn("[도서 등록] 해당 작가 정보가 비어있습니다.");
            return;
        }

        Map<String, Author> authorMap = authorRepository.findAllByNameIn(authorReqDTOs.stream()
                        .map(AuthorReqDTO::authorName)
                        .toList()).stream()
                .collect(Collectors.toMap(Author::getName, author -> author));

        Map<String, Role> roleMap = roleRepository.findAllByNameIn(authorReqDTOs.stream()
                        .map(AuthorReqDTO::roleName)
                        .toList()).stream()
                .collect(Collectors.toMap(Role::getName, role -> role));

        List<Author> saveAuthors = new ArrayList<>();
        List<BookAuthor> bookAuthors = new ArrayList<>();
        for(AuthorReqDTO a: authorReqDTOs){
            Author author;
            if(authorMap.containsKey(a.authorName())){
                author = authorMap.get(a.authorName());
            } else {
                author = new Author(a.authorName());
                saveAuthors.add(author);
            }

            Role role = null;
            if(a.roleName() != null && roleMap.containsKey(a.roleName())){
                role = roleMap.get(a.roleName());
            }

            BookAuthor newBookAuthor = new BookAuthor(book, author);
            book.getBookAuthors().add(newBookAuthor);
            author.getBookAuthors().add(newBookAuthor);

            if(role != null){
                newBookAuthor.setRole(role);
                role.getBookAuthors().add(newBookAuthor);
            }

            bookAuthors.add(newBookAuthor);
        }

        if(!saveAuthors.isEmpty()){
            authorRepository.saveAll(saveAuthors);
        }
        bookAuthorRepository.saveAll(bookAuthors);
    }

    @Override
    @Transactional
    public void assignAuthorsToBooks(Map<String, Book> bookMap, Map<String, List<AuthorReqDTO>> authorListMap) {
        Map<String, Author> authorMap = authorRepository.findAllByNameIn(authorListMap.values().stream()
                        .flatMap(a -> a.stream().map(AuthorReqDTO::authorName))
                        .toList()).stream()
                .collect(Collectors.toMap(Author::getName, author -> author));

        Map<String, Role> roleMap = roleRepository.findAllByNameIn(authorListMap.values().stream()
                        .flatMap(a -> a.stream().map(AuthorReqDTO::roleName))
                        .toList()).stream()
                .collect(Collectors.toMap(Role::getName, role -> role));

        List<Author> saveAuthors = new ArrayList<>();
        List<BookAuthor> bookAuthors = new ArrayList<>();

        for(Book book: bookMap.values()){
            List<AuthorReqDTO> authorReqDTOS = authorListMap.get(book.getIsbn());

            for(AuthorReqDTO a: authorReqDTOS){
                Author author;
                if(authorMap.containsKey(a.authorName())){
                    author = authorMap.get(a.authorName());
                } else {
                    author = new Author(a.authorName());
                    authorMap.put(author.getName(), author);
                    saveAuthors.add(author);
                }

                Role role = null;
                if(a.roleName() != null && roleMap.containsKey(a.roleName())){
                    role = roleMap.get(a.roleName());
                }

                BookAuthor newBookAuthor = new BookAuthor(book, author);
                book.getBookAuthors().add(newBookAuthor);
                author.getBookAuthors().add(newBookAuthor);

                if(role != null){
                    newBookAuthor.setRole(role);
                    role.getBookAuthors().add(newBookAuthor);
                }

                bookAuthors.add(newBookAuthor);
            }
        }

        if(!saveAuthors.isEmpty()){
            authorRepository.saveAll(saveAuthors);
        }
        bookAuthorRepository.saveAll(bookAuthors);
    }

    @Override
    @Transactional
    public void updateAuthorOfBook(Book book, List<AuthorReqDTO> authorReqDTOs) {
        //작가 체크
//        Map<String, String> author = book.getBookAuthors().stream()
//                .collect(Collectors.toMap(ba -> ba.getAuthor().getName(),
//                        ba -> ba.getRole() != null ? ba.getRole().getName() : ""));
//
//        boolean checkAuthor = false;
//        Set<String> authorName = author.keySet();
//        for(AuthorReqDTO ar : authorReqDTOs){
//            if(authorName.contains(ar.authorName())){
//                if(author.get(ar.authorName()).equals(ar.roleNames()) ||
//                        author.get(ar.authorName()).isEmpty() && ar.roleNames() == null){
//                    continue;
//                }
//                checkAuthor = true;
//                break;
//            }
//        }

        Set<String> updateAuthorNames = authorReqDTOs.stream()
                .map(AuthorReqDTO::authorName)
                .collect(Collectors.toSet());

        List<BookAuthor> bookAuthorsToDelete = book.getBookAuthors().stream()
                .filter(ba -> !updateAuthorNames.contains(ba.getAuthor().getName())) // 요청 목록에 없는 작가만 필터링
                .toList();

        if (!bookAuthorsToDelete.isEmpty()) {
            bookAuthorsToDelete.forEach(ba -> {
                ba.getAuthor().getBookAuthors().remove(ba); // Author 측 관계 제거
                if (ba.getRole() != null) {
                    ba.getRole().getBookAuthors().remove(ba); // Role 측 관계 제거
                }
            });

            book.getBookAuthors().removeAll(bookAuthorsToDelete);

            bookAuthorRepository.deleteAllById(bookAuthorsToDelete.stream()
                    .map(BookAuthor::getId)
                    .toList());
        }

        Map<String, BookAuthor> existingBookAuthorMap = book.getBookAuthors().stream()
                .collect(Collectors.toMap(
                        ba -> ba.getAuthor().getName(),
                        ba -> ba
                ));

        Map<String, Role> roleMap = roleRepository.findAllByNameIn(authorReqDTOs.stream()
                        .map(AuthorReqDTO::roleName)
                        .toList()).stream()
                .collect(Collectors.toMap(Role::getName, role -> role));
        Map<String, Author> authorMap = authorRepository.findAllByNameIn(authorReqDTOs.stream()
                        .map(AuthorReqDTO::authorName)
                        .toList()).stream()
                .collect(Collectors.toMap(Author::getName, author -> author));

        List<Author> authorList = new ArrayList<>();
        List<BookAuthor> bookAuthorList = new ArrayList<>();
        for (AuthorReqDTO a : authorReqDTOs) {
            Role role = a.roleName() != null && roleMap.containsKey(a.roleName()) ? roleMap.get(a.roleName()) : null;

            BookAuthor existingBookAuthor = existingBookAuthorMap.get(a.authorName());

            if (existingBookAuthor != null) { // 관계가 있으면 명칭이 변경이 있는지만 확인
                Role currentRole = existingBookAuthor.getRole();

                if ((role == null && currentRole != null) || //명칭이 null -> notNull
                        (role != null && currentRole == null) || //명칭이 notNull -> null
                        (role != null && currentRole != null && role.getId() != currentRole.getId())) { //명칭이 변경

                    if (currentRole != null) {
                        currentRole.getBookAuthors().remove(existingBookAuthor);
                    }

                    existingBookAuthor.setRole(role);
                    if (role != null) {
                        role.getBookAuthors().add(existingBookAuthor);
                    }
                }
            } else { // 관계가 없으면 새로운 작가를 등록
                Author author = a.authorName() != null && authorMap.containsKey(a.authorName()) ? authorMap.get(a.authorName()) : null;
                if(author == null){
                    author = new Author(a.authorName());
                    authorList.add(author);
                }

                BookAuthor newBookAuthor = new BookAuthor(book, author);
                bookAuthorList.add(newBookAuthor);

                if (role != null) {
                    newBookAuthor.setRole(role);
                    role.getBookAuthors().add(newBookAuthor);
                }

                book.getBookAuthors().add(newBookAuthor);
                author.getBookAuthors().add(newBookAuthor);
            }
        }
        if(!authorList.isEmpty()){
            authorRepository.saveAll(authorList);
        }
        if(!bookAuthorList.isEmpty()){
            bookAuthorRepository.saveAll(bookAuthorList);
        }
    }

    @Override
    @Transactional
    public void deleteAuthorOfBook(Book book) {
        List<BookAuthor> bookAuthors = book.getBookAuthors();
        Set<Author> authors = bookAuthors.stream()
                .map(BookAuthor::getAuthor)
                .collect(Collectors.toSet());
        Set<Role> roles = bookAuthors.stream()
                .map(BookAuthor::getRole)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if(!authors.isEmpty()) {
            authors.forEach(a -> a.getBookAuthors().removeAll(bookAuthors));
        }
        if(!roles.isEmpty()) {
            roles.forEach(r -> r.getBookAuthors().removeAll(bookAuthors));
        }
        book.getBookAuthors().removeAll(bookAuthors);

        bookAuthorRepository.deleteAll(bookAuthors);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleNameListRespDTO getRoleNameList(){
        return new RoleNameListRespDTO(roleRepository.getAllRoleName().stream()
                .map(RoleNameProjection::getRoleName)
                .toList());
    }
}
