package com.daisobook.shop.booksearch.books_search.dto.response;

public record AuthorRespDTO(
        Long authorId,
        String authorName,
        Long roleId,
        String roleName) {
}
