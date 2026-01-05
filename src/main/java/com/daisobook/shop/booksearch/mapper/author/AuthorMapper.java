package com.daisobook.shop.booksearch.mapper.author;

import com.daisobook.shop.booksearch.dto.response.AuthorRespDTO;
import com.daisobook.shop.booksearch.entity.author.BookAuthor;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
import java.util.Map;

public interface AuthorMapper {
    List<AuthorRespDTO> toAuthorRespDTOList(List<BookAuthor> bookAuthors);
    List<AuthorRespDTO> toAuthorRespDTOList(String authorsData) throws JsonProcessingException;
    Map<Long, List<AuthorRespDTO>> toAuthorRespDTOMap(Map<Long, String> authorDataMap) throws JsonProcessingException;
}
