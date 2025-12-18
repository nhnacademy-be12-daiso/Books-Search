package com.daisobook.shop.booksearch.BooksSearch.mapper.author.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.response.AuthorRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.author.BookAuthor;
import com.daisobook.shop.booksearch.BooksSearch.mapper.author.AuthorMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class AuthorMapperImpl implements AuthorMapper {
    private final ObjectMapper objectMapper;

    @Override
    public List<AuthorRespDTO> toAuthorRespDTOList(List<BookAuthor> bookAuthors) {
        return bookAuthors.stream()
                .map(ba ->
                        new AuthorRespDTO(ba.getAuthor() != null ? ba.getAuthor().getId() : null,
                                ba.getAuthor() != null ? ba.getAuthor().getName() : null,
                                ba.getRole() != null ? ba.getRole().getId() : null,
                                ba.getRole() != null ? ba.getRole().getName() : null))
                .toList();
    }

    @Override
    public Map<Long, List<AuthorRespDTO>> toAuthorRespDTOMap(Map<Long, String> authorDataMap) throws JsonProcessingException {
        Set<Long> keySet = authorDataMap.keySet();

        Map<Long, List<AuthorRespDTO>> listMap = new HashMap<>();
        for(Long key: keySet){
            if(!authorDataMap.containsKey(key)){
                continue;
            }
            String s = authorDataMap.get(key);
            List<AuthorRespDTO> authorRespDTOList = objectMapper.readValue(s, new TypeReference<List<AuthorRespDTO>>() {});
            listMap.put(key, authorRespDTOList);
        }
        return listMap;
    }
}
