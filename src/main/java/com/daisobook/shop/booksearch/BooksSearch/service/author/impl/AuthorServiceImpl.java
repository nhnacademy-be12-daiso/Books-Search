//package com.daisobook.shop.booksearch.BooksSearch.service.author.impl;
//
//import com.daisobook.shop.booksearch.BooksSearch.dto.request.AuthorReqDTO;
//import com.daisobook.shop.booksearch.BooksSearch.dto.response.AuthorRespDTO;
//import com.daisobook.shop.booksearch.BooksSearch.entity.author.Author;
//import com.daisobook.shop.booksearch.BooksSearch.entity.author.BookAuthor;
//import com.daisobook.shop.booksearch.BooksSearch.entity.author.Role;
//import com.daisobook.shop.booksearch.BooksSearch.exception.custom.author.*;
//import com.daisobook.shop.booksearch.BooksSearch.repository.author.AuthorRepository;
//import com.daisobook.shop.booksearch.BooksSearch.repository.author.BookAuthorRepository;
//import com.daisobook.shop.booksearch.BooksSearch.repository.author.RoleRepository;
//import com.daisobook.shop.booksearch.BooksSearch.service.author.AuthorService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//@Slf4j
//@RequiredArgsConstructor
//@Service
//public class AuthorServiceImpl implements AuthorService {
//    private final AuthorRepository authorRepository;
//    private final RoleRepository roleRepository;
//    private final BookAuthorRepository bookAuthorRepository;
//
//    @Transactional
//    @Override
//    public void registerAuthor(AuthorReqDTO authorReqDTO){
//        if(authorRepository.existsByName(authorReqDTO.authorName())){
//            log.error("이미 존재하는 작가 이름입니다 - authorName:{}", authorReqDTO.authorName());
//            throw new DuplicatedAuthor("이미 존재하는 작가 이름입니다");
//        }
//
//        authorRepository.save(Author.create(authorReqDTO));
//    }
//
//    @Transactional
//    @Override
//    public void registerRole(AuthorReqDTO authorReqDTO){
//        if(roleRepository.existsRoleByName(authorReqDTO.roleName())){
//            log.error("이미 존재하는 명칭입니다 - roleName:{}", authorReqDTO.roleName());
//            throw new DuplicatedRole("이미 존재하는 명칭입니다");
//        }
//
//        roleRepository.save(Role.create(authorReqDTO));
//    }
//
//    @Transactional
//    @Override
//    public void registerRoles(List<AuthorReqDTO> authorReqDTOList){
//        Set<String> roleNameMap = roleRepository.findAllByNameIn(authorReqDTOList.stream()
//                        .map(AuthorReqDTO::authorName)
//                        .toList()).stream()
//                .map(Role::getName)
//                .collect(Collectors.toSet());
//
//        for(AuthorReqDTO a: authorReqDTOList){
//            if(roleNameMap.contains(a.roleName())){
//                log.error("이미 존재하는 명칭 등록 시도 - 명칭:{}", a.roleName());
//                continue;
//            }
//
//            roleRepository.save(Role.create(a));
//        }
//    }
//
//    @Transactional
//    @Override
//    public void updateAuthor(long authorId, AuthorReqDTO authorReqDTO){
//        Author author = authorRepository.findAuthorById(authorId);
//        if(author == null){
//            log.error("해당 아이디를 가진 작가를 찾지 못 했습니다 - authorId:{}", authorId);
//            throw new NotFoundAuthor("해당 아이디를 가진 작가를 찾지 못 했습니다");
//        }
//
//        if(authorRepository.existsByName(authorReqDTO.authorName())){
//            log.error("변경할 이름이 이미 존재하는 이름입니다 - authorName:{}", authorReqDTO.authorName());
//            throw new CannotChangedAuthor("변경할 이름이 이미 존재하는 이름입니다");
//        }
//
//        author.setName(authorReqDTO.authorName());
//    }
//
//    @Transactional
//    @Override
//    public void updateRole(long roleId, AuthorReqDTO authorReqDTO){
//        Role role = roleRepository.findRoleById(roleId);
//        if(role == null){
//            log.error("해당 아이디를 가진 명칭를 찾지 못 했습니다 - authorId:{}", roleId);
//            throw new NotFoundRole("해당 아이디를 가진 명칭를 찾지 못 했습니다");
//        }
//
//        if(roleRepository.existsRoleByName(authorReqDTO.roleName())){
//            log.error("변경할 명칭이 이미 존재하는 명칭입니다 - roleName:{}", authorReqDTO.roleName());
//            throw new CannotChangedRole("변경할 이름이 이미 존재하는 이름입니다");
//        }
//
//        role.setName(authorReqDTO.roleName());
//    }
//
//    @Transactional
//    @Override
//    public AuthorRespDTO getAuthorByAuthorId(long authorId){
//        Author author = authorRepository.findAuthorById(authorId);
//        if(author == null){
//            log.error("해당 아이디를 가진 작가를 찾지 못 했습니다 - authorId:{}", authorId);
//            throw new NotFoundAuthor("해당 아이디를 가진 작가를 찾지 못 했습니다");
//        }
//
//        return new AuthorRespDTO(author.getId(), author.getName(), null, null);
//    }
//
//    @Transactional
//    @Override
//    public AuthorRespDTO getAuthorByRoleId(long roleId){
//        Role role = roleRepository.findRoleById(roleId);
//        if(role == null){
//            log.error("해당 아이디를 가진 명칭를 찾지 못 했습니다 - authorId:{}", roleId);
//            throw new NotFoundRole("해당 아이디를 가진 명칭를 찾지 못 했습니다");
//        }
//
//        return new AuthorRespDTO(null, null, role.getId(), role.getName());
//    }
//
//    @Override
//    public List<AuthorRespDTO> getAuthorsByRoleId(long roleId) {
//        Role role = roleRepository.findRoleById(roleId);
//        if(role == null){
//            log.error("해당 아이디를 가진 명칭를 찾지 못 했습니다 - authorId:{}", roleId);
//            throw new NotFoundRole("해당 아이디를 가진 명칭를 찾지 못 했습니다");
//        }
//
//        List<BookAuthor> bookAuthors = bookAuthorRepository.findAllByRole_Id(roleId);
//        if(bookAuthors.isEmpty()) {
//            return List.of();
//        }
//        return bookAuthors.stream()
//                .map(BookAuthor::getAuthor)
//                .map(a -> new AuthorRespDTO(a.getId(), a.getName(), role.getId(), role.getName()))
//                .toList();
//    }
//
//    @Transactional
//    @Override
//    public void deleteAuthor(long authorId){
//        if(bookAuthorRepository.existsByAuthor_Id(authorId)){
//            log.error("해당 작가에 관계가 존재하여 삭제 불가 - authorId:{}", authorId);
//            throw new CannotChangedRole("해당 작가에 관계가 존재하여 삭제 불가");
//        }
//
//        authorRepository.deleteById(authorId);
//    }
//
//    @Transactional
//    @Override
//    public void deleteRole(long roleId){
//        if(bookAuthorRepository.existsByRole_Id(roleId)){
//            log.error("해당 명칭에 관계가 존재하여 삭제 불가 - roleId:{}", roleId);
//            throw new CannotChangedRole("해당 명칭에 관계가 존재하여 삭제 불가");
//        }
//
//        roleRepository.deleteById(roleId);
//    }
//
//    @Override
//    public Author findAuthorByName(String name){
//        return authorRepository.findAuthorByName(name);
//    }
//
//    @Override
//    public Role findRoleByName(String name){
//        return roleRepository.findRoleByName(name);
//    }
//
//    @Override
//    public List<Author> findAuthorsByNameIn(List<String> authorNames){
//        List<Author> authors = authorRepository.findAllByNameIn(authorNames);
//        if(authors.isEmpty()) {
//            return List.of();
//        }
//
//        return authors;
//    }
//
//    @Override
//    public List<Role> findRolesByNameIn(List<String> roleNames) {
//        List<Role> roles = roleRepository.findAllByNameIn(roleNames);
//        if(roles.isEmpty()) {
//            return List.of();
//        }
//
//        return roles;
//    }
//}
