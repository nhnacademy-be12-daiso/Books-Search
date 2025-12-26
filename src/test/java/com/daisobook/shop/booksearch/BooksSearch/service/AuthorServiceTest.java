package com.daisobook.shop.booksearch.BooksSearch.service;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.AuthorReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.author.Author;
import com.daisobook.shop.booksearch.BooksSearch.entity.author.Role;
import com.daisobook.shop.booksearch.BooksSearch.repository.author.AuthorRepository;
import com.daisobook.shop.booksearch.BooksSearch.repository.author.BookAuthorRepository;
import com.daisobook.shop.booksearch.BooksSearch.repository.author.RoleRepository;
//import com.daisobook.shop.booksearch.BooksSearch.service.author.impl.AuthorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthorServiceTest {
//    @InjectMocks
//    private AuthorServiceImpl authorService;

    @MockitoBean
    private AuthorRepository authorRepository;
    @MockitoBean
    private RoleRepository roleRepository;
    @MockitoBean
    private BookAuthorRepository bookAuthorRepository;

    private AuthorReqDTO authorReqDTO;
    private Author author;
    private Role role;

    @BeforeEach
    void setUp() {
        authorReqDTO = new AuthorReqDTO("testAuthor", "testRole");
        author = Author.create(authorReqDTO);
        role = new Role("testRole");
    }

    @Test
    @DisplayName("작가 등록 성공")
    void registerAuthor_Success() {
        when(authorRepository.existsByName(anyString())).thenReturn(false);

//        assertDoesNotThrow(() -> authorService.registerAuthor(authorReqDTO));
        verify(authorRepository, times(1)).save(any(Author.class));
    }
}
