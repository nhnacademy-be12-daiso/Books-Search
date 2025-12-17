package com.daisobook.shop.booksearch.BooksSearch.mapper.tag;

import com.daisobook.shop.booksearch.BooksSearch.dto.response.TagRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.tag.BookTag;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
import java.util.Map;

public interface TagMapper {
    List<TagRespDTO> toTagRespDTOList(List<BookTag> bookTags);
    Map<Long, List<TagRespDTO>> toTagRespDTOMap(Map<Long, String> tagDataMap) throws JsonProcessingException;
}
