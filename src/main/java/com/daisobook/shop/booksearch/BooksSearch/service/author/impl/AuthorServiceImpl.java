package com.daisobook.shop.booksearch.BooksSearch.service.author.impl;

import com.daisobook.shop.booksearch.BooksSearch.repository.AuthorRepository;
import com.daisobook.shop.booksearch.BooksSearch.repository.BookAuthorRepository;
import com.daisobook.shop.booksearch.BooksSearch.repository.RoleRepository;
import com.daisobook.shop.booksearch.BooksSearch.service.author.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthorServiceImpl implements AuthorService {
    private final AuthorRepository authorRepository;
    private final RoleRepository roleRepository;
    private final BookAuthorRepository bookAuthorRepository;
}
