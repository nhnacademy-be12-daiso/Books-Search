package com.daisobook.shop.booksearch.BooksSearch.service.like.impl;

import com.daisobook.shop.booksearch.BooksSearch.repository.LikeRepository;
import com.daisobook.shop.booksearch.BooksSearch.service.like.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;


}
