package com.daisobook.shop.booksearch.search.message;

import java.time.LocalDate;
import java.util.List;

public record BookUpsertMessage(
        String requestId,
        BookPayload book,
        long ts,
        String reason
) {
    public record BookPayload(
            String isbn,
            Long id,
            String title,
            String author,
            String publisher,
            String description,
            LocalDate pubDate,
            Integer price,
            List<String> categories,
            String imageUrl,
            Long publisherId,
            Long categoryId
    ) {}
}
