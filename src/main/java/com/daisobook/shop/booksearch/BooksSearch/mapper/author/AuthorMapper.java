package com.daisobook.shop.booksearch.BooksSearch.mapper.author;

import com.daisobook.shop.booksearch.BooksSearch.dto.response.AuthorRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.author.BookAuthor;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
import java.util.Map;

public interface AuthorMapper {
    List<AuthorRespDTO> toAuthorRespDTOList(List<BookAuthor> bookAuthors);
    Map<Long, List<AuthorRespDTO>> toAuthorRespDTOMap(Map<Long, String> authorDataMap) throws JsonProcessingException;
}
