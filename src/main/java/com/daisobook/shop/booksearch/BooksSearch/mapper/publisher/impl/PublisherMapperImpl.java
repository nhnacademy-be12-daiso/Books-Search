package com.daisobook.shop.booksearch.BooksSearch.mapper.publisher.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.response.PublisherRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.mapper.publisher.PublisherMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class PublisherMapperImpl implements PublisherMapper {
    private final ObjectMapper objectMapper;

    @Override
    public Map<Long, PublisherRespDTO> toPublisherRespDTOMap(Map<Long, String> publisherDataMap) throws JsonProcessingException {
        Set<Long> keySet = publisherDataMap.keySet();

        Map<Long, PublisherRespDTO> listMap = new HashMap<>();
        for(Long key: keySet){
            if(!publisherDataMap.containsKey(key)){
                continue;
            }
            String s = publisherDataMap.get(key);
            PublisherRespDTO publisherRespDTO = objectMapper.readValue(s, PublisherRespDTO.class);
            listMap.put(key, publisherRespDTO);
        }
        return listMap;
    }
}
