package com.daisobook.shop.booksearch.BooksSearch.mapper.like.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.DiscountDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.DiscountValueListData;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.LikeBookListProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.AuthorRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.like.LikeListRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.mapper.author.AuthorMapper;
import com.daisobook.shop.booksearch.BooksSearch.mapper.like.LikeMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class LikeMapperImpl implements LikeMapper {
    private final AuthorMapper authorMapper;

    @Override
    public List<LikeListRespDTO> toLikeListRespDTOList(List<LikeBookListProjection> listProjectionList, Map<Long, DiscountDTO.Response> discountResponseMap) throws JsonProcessingException {
        Map<Long, List<AuthorRespDTO>> authorsMap = authorMapper.toAuthorRespDTOMap(listProjectionList.stream()
                .collect(Collectors.toMap(LikeBookListProjection::getBookId, LikeBookListProjection::getAuthors)));

        return listProjectionList.stream()
                .map(ll -> new LikeListRespDTO(ll.getLikeId(), ll.getBookId(), ll.getIsbn(),
                        ll.getTitle(), authorsMap.getOrDefault(ll.getBookId(), null), ll.getPrice(),
                        discountResponseMap.containsKey(ll.getBookId()) ? discountResponseMap.get(ll.getBookId()).discountPercentage() : null,
                        discountResponseMap.containsKey(ll.getBookId()) ? discountResponseMap.get(ll.getBookId()).discountPrice() : null,
                        ll.getStatus(), ll.getImage(), ll.getVolumeNo(), ll.getPackaging(), ZonedDateTime.parse(ll.getCreatedAt())))
                .toList();
    }

    @Override
    public Map<Long, DiscountDTO.Request> toDiscountDTOMap(List<LikeBookListProjection> listProjectionList) {
        return listProjectionList.stream()
                .map(lp -> new DiscountDTO.Request(lp.getBookId(), lp.getPrice()))
                .collect(Collectors.toMap(DiscountDTO.Request::bookId, request -> request));
    }
}
