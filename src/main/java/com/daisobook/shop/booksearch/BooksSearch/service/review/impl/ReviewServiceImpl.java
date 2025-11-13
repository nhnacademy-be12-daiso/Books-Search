package com.daisobook.shop.booksearch.BooksSearch.service.review.impl;

import com.daisobook.shop.booksearch.BooksSearch.repository.ReviewRepository;
import com.daisobook.shop.booksearch.BooksSearch.service.review.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;


}
