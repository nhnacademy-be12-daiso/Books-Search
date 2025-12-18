package com.daisobook.shop.booksearch.BooksSearch.mapper.publisher;

import com.daisobook.shop.booksearch.BooksSearch.dto.response.PublisherRespDTO;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Map;

public interface PublisherMapper {
    Map<Long, PublisherRespDTO> toPublisherRespDTOMap(Map<Long, String> publisherDataMap) throws JsonProcessingException;
    PublisherRespDTO toPublisherRespDTO(String publisherData) throws JsonProcessingException;
}
