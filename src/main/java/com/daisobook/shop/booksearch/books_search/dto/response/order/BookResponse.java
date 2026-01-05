package com.daisobook.shop.booksearch.books_search.dto.response.order;

import com.daisobook.shop.booksearch.books_search.dto.response.ImageRespDTO;

import java.util.List;

public record BookResponse(
        long bookId,
        String title,
        List<ImageRespDTO> imageList
) {}