package com.daisobook.shop.booksearch.BooksSearch.service.policy.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.BookListData;
import com.daisobook.shop.booksearch.BooksSearch.dto.DiscountValueListData;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.DiscountValueListProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.DiscountValueProjection;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.policy.DiscountType;
import com.daisobook.shop.booksearch.BooksSearch.entity.policy.TargetType;
import com.daisobook.shop.booksearch.BooksSearch.repository.policy.DiscountPolicyRepository;
import com.daisobook.shop.booksearch.BooksSearch.service.policy.DiscountPolicyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class DiscountPolicyServiceImpl implements DiscountPolicyService {
    private final DiscountPolicyRepository discountPolicyRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    @Override
    public List<DiscountValueProjection> getDiscountValueByBookIdOrCategoryIdsOrPublisherId(List<Long> categoryIds, Long publisherId, Long bookId){
        List<DiscountValueProjection> discountPolicies = new ArrayList<>(discountPolicyRepository.findAllByTargetTypeAndTargetIdAndIsActive(TargetType.GLOBAL, null, true));

        if(categoryIds != null && !categoryIds.isEmpty()){
            discountPolicies.addAll(discountPolicyRepository.findAllByTargetTypeAndTargetIdInAndIsActive(TargetType.CATEGORY, categoryIds, true));
        }
        if(publisherId != null) {
            discountPolicies.addAll(discountPolicyRepository.findAllByTargetTypeAndTargetIdAndIsActive(TargetType.PUBLISHER, publisherId, true));
        }
        if(bookId != null) {
            discountPolicies.addAll(discountPolicyRepository.findAllByTargetTypeAndTargetIdAndIsActive(TargetType.PRODUCT, bookId, true));
        }

        return discountPolicies;
    }

    @Transactional
    @Override
    public Long getDiscountPrice(Book book){
        List<DiscountValueProjection> discountPolicyList = getDiscountValueByBookIdOrCategoryIdsOrPublisherId(
                book.getBookCategories().stream()
                        .map(bc -> bc.getCategory().getId()).toList(),
                book.getPublisher().getId(),
                book.getId());

        if(discountPolicyList == null || discountPolicyList.isEmpty()){
            return null;
        }

        long discountPercentage = 0;
        long discountFixedAmount = 0;
        for(DiscountValueProjection dp: discountPolicyList){
            if(DiscountType.PERCENTAGE.equals(dp.getDiscountType())){
                discountPercentage += dp.getValue();
            } else if(DiscountType.FIXED_AMOUNT.equals(dp.getDiscountType())){
                discountFixedAmount += dp.getValue();
            }
        }

        Long price = book.getPrice();
        if(price == null){
            log.warn("해당 도서의 가격이 존재하지 않습니다 - bookId:{}", book.getId());
            return null;
        }
        if(discountPercentage > 0){
            price = (long) (price * (1 - discountPercentage / 100.0));
        }
        if(discountFixedAmount > 0){
            price = price - discountFixedAmount;
        }

        return price < 0 ? 0: price;
    }

    @Transactional
    @Override
    public Map<Long, List<DiscountValueListData>> getDiscountPolicyByDataMap(List<Long> bookIds) throws JsonProcessingException {
        List<DiscountValueListProjection> discountValueList = discountPolicyRepository.getDiscountValue(bookIds);

        Map<Long, List<DiscountValueListData>> discountMap = new HashMap<>();
        for(DiscountValueListProjection dv: discountValueList){
            List<DiscountValueListData> discountValueListProjections = objectMapper.readValue(dv.getDiscountValueList(), new TypeReference<List<DiscountValueListData>>() {});
            discountMap.put(dv.getBookId(), discountValueListProjections);
        }
        return discountMap;
    }

    @Transactional
    @Override
    public Map<Long, Long> getDiscountPriceMap(Map<Long, BookListData> bookListDataMap) throws JsonProcessingException {
        Set<Long> bookIdSet = bookListDataMap.values().stream()
                .map(BookListData::getId)
                .collect(Collectors.toSet());

        Map<Long, List<DiscountValueListData>> discountPolicyByDataMap = getDiscountPolicyByDataMap(bookIdSet.stream().toList());

        Map<Long, Long> discountPriceMap = new HashMap<>();
        for(Long bookId: bookIdSet){
            if(!discountPolicyByDataMap.containsKey(bookId)){
                continue;
            }

            List<DiscountValueListData> discountValueListData = discountPolicyByDataMap.get(bookId);
            if(discountValueListData == null || discountValueListData.isEmpty()){
                continue;
            }

            BookListData bookListData = bookListDataMap.get(bookId);

            long discountPercentage = 0;
            long discountFixedAmount = 0;
            for(DiscountValueListData dv: discountValueListData){
                if(DiscountType.PERCENTAGE.equals(dv.getDiscountType())){
                    discountPercentage += dv.getValue();
                } else if(DiscountType.FIXED_AMOUNT.equals(dv.getDiscountType())){
                    discountFixedAmount += dv.getValue();
                }
            }

            Long price = bookListData.getPrice();
            if(price == null){
                log.warn("[도서 목록] 해당 도서의 가격이 존재하지 않습니다 - bookId:{}", bookListData.getId());
                return null;
            }
            if(discountPercentage > 0){
                price = (long) (price * (1 - discountPercentage / 100.0));
            }
            if(discountFixedAmount > 0){
                price = price - discountFixedAmount;
            }

            long discountPrice = price < 0 ? 0: price;
            bookListData.setDiscountPrice(discountPrice);

            BigDecimal i = bookListData.getPrice() != null ? BigDecimal.valueOf((1.0 - (double) discountPrice / bookListData.getPrice()) * 100.0): null;
            bookListData.setDiscountPercentage(i != null ? i.setScale(2, RoundingMode.DOWN) : null);
            
            discountPriceMap.put(bookId, price < 0 ? 0: price);
        }

        return discountPriceMap;
    }

}
