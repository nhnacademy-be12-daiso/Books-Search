package com.daisobook.shop.booksearch.BooksSearch.dto.response.order;

import com.daisobook.shop.booksearch.BooksSearch.dto.response.ImageRespDTO;

import java.util.List;

public record BookResponse(
        long bookId,
        String title,
        List<ImageRespDTO> imageList
) {}