package com.daisobook.shop.booksearch.BooksSearch.search.dto;


import com.daisobook.shop.booksearch.BooksSearch.search.domain.Book;

public record BookWithScore(Book book, double score) {}
