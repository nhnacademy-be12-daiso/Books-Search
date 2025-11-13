package com.daisobook.shop.booksearch.BooksSearch.service.impl;

import com.daisobook.shop.booksearch.BooksSearch.repository.BookRepository;
import com.daisobook.shop.booksearch.BooksSearch.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;


}
