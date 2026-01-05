package com.daisobook.shop.booksearch.dto.response;

public record AuthorRespDTO(
        Long authorId,
        String authorName,
        Long roleId,
        String roleName) {
}
