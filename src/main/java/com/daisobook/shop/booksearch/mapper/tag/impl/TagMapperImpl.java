package com.daisobook.shop.booksearch.mapper.tag.impl;

import com.daisobook.shop.booksearch.dto.response.TagRespDTO;
import com.daisobook.shop.booksearch.entity.tag.BookTag;
import com.daisobook.shop.booksearch.mapper.tag.TagMapper;
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
public class TagMapperImpl implements TagMapper {
    private final ObjectMapper objectMapper;

    @Override
    public List<TagRespDTO> toTagRespDTOList(List<BookTag> bookTags) {
        return bookTags.stream()
                .map(BookTag::getTag)
                .map(t -> new TagRespDTO(t.getId(), t.getName()))
                .toList();
    }

    @Override
    public List<TagRespDTO> toTagRespDTOList(String tagsData) throws JsonProcessingException {
        if(tagsData == null || tagsData.isBlank()){
            return null;
        }

        return objectMapper.readValue(tagsData, new TypeReference<List<TagRespDTO>>() {});
    }

    @Override
    public Map<Long, List<TagRespDTO>> toTagRespDTOMap(Map<Long, String> tagDataMap) throws JsonProcessingException {
        Set<Long> keySet = tagDataMap.keySet();

        Map<Long, List<TagRespDTO>> listMap = new HashMap<>();
        for(Long key: keySet){
            if(!tagDataMap.containsKey(key)){
                continue;
            }
            String s = tagDataMap.get(key);
            List<TagRespDTO> tagRespDTOList = objectMapper.readValue(s, new TypeReference<List<TagRespDTO>>() {});
            listMap.put(key, tagRespDTOList);
        }
        return listMap;
    }
}
