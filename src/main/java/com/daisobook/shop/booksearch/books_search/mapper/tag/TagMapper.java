package com.daisobook.shop.booksearch.books_search.mapper.tag;

import com.daisobook.shop.booksearch.books_search.dto.response.TagRespDTO;
import com.daisobook.shop.booksearch.books_search.entity.tag.BookTag;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
import java.util.Map;

public interface TagMapper {
    List<TagRespDTO> toTagRespDTOList(List<BookTag> bookTags);
    List<TagRespDTO> toTagRespDTOList(String tagsData) throws JsonProcessingException;
    Map<Long, List<TagRespDTO>> toTagRespDTOMap(Map<Long, String> tagDataMap) throws JsonProcessingException;
}
