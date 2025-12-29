package com.daisobook.shop.booksearch.BooksSearch.service.policy.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.DiscountDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.DiscountValueListData;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookDetailProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.DiscountValueListProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.DiscountValueProjection;
import com.daisobook.shop.booksearch.BooksSearch.entity.policy.DiscountPolicy;
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
    public List<DiscountValueListData> getDiscountPolicyByData(Long bookId) throws JsonProcessingException {
        List<DiscountValueListProjection> discountValueList = discountPolicyRepository.getDiscountValue(new ArrayList<>(Collections.singleton(bookId)));
        List<DiscountValueListData> discountList = new ArrayList<>();
        for(DiscountValueListProjection dv: discountValueList){
            if(dv == null){
                continue;
            }
            List<DiscountValueListData> discountValueListProjections = objectMapper.readValue(dv.getDiscountValueList(), new TypeReference<List<DiscountValueListData>>() {});
            discountList.addAll(discountValueListProjections);
        }
        return discountList;
    }

    @Transactional
    @Override
    public Long getDiscountPrice(long bookId, Long price) throws JsonProcessingException {
        if(price == null){
            return null;
        }
        List<DiscountValueListData> discountPolicyList = getDiscountPolicyByData(bookId);

        if(discountPolicyList == null || discountPolicyList.isEmpty()){
            return null;
        }

        long discountPercentage = 0;
        long discountFixedAmount = 0;
        for(DiscountValueListData dl : discountPolicyList){
            if(DiscountType.PERCENTAGE.equals(dl.getDiscountType())){
                discountPercentage += dl.getValue();
            } else if(DiscountType.FIXED_AMOUNT.equals(dl.getDiscountType())){
                discountFixedAmount += dl.getValue();
            }
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
    public Map<Long, DiscountDTO.Response> getDiscountPriceMap(Map<Long, DiscountDTO.Request> discountDTORequestMap) throws JsonProcessingException {
        Set<Long> bookIdSet = new HashSet<>(discountDTORequestMap.keySet());

        Map<Long, List<DiscountValueListData>> discountPolicyByDataMap = getDiscountPolicyByDataMap(bookIdSet.stream().toList());

        Map<Long, DiscountDTO.Response> discountPriceMap = new HashMap<>();
        for(Long bookId: bookIdSet){
            if(!discountPolicyByDataMap.containsKey(bookId)){
                continue;
            }

            List<DiscountValueListData> discountValueListData = discountPolicyByDataMap.get(bookId);
            if(discountValueListData == null || discountValueListData.isEmpty()){
                continue;
            }

            DiscountDTO.Request discountRequest = discountDTORequestMap.get(bookId);
            DiscountDTO.Response discountResponse = null;

            long discountPercentage = 0;
            long discountFixedAmount = 0;
            for(DiscountValueListData dv: discountValueListData){
                if(DiscountType.PERCENTAGE.equals(dv.getDiscountType())){
                    discountPercentage += dv.getValue();
                } else if(DiscountType.FIXED_AMOUNT.equals(dv.getDiscountType())){
                    discountFixedAmount += dv.getValue();
                }
            }

            Long price = discountRequest.price();
            if(price == null){
                log.warn("[도서 목록] 해당 도서의 가격이 존재하지 않습니다 - bookId:{}", discountRequest.bookId());
                return null;
            }
            if(discountPercentage > 0){
                price = (long) (price * (1 - discountPercentage / 100.0));
            }
            if(discountFixedAmount > 0){
                price = price - discountFixedAmount;
            }

            long discountPrice = price < 0 ? 0: price;

            BigDecimal i = discountRequest.price() != null ? BigDecimal.valueOf((1.0 - (double) discountPrice / discountRequest.price()) * 100.0): null;

            discountResponse = new DiscountDTO.Response(bookId, price, i != null ? i.setScale(2, RoundingMode.DOWN) : null, discountPrice);
            
            discountPriceMap.put(bookId, discountResponse);
        }

        return discountPriceMap;
    }

    @Transactional
    @Override
    public List<DiscountPolicy> getAllActiveDiscountPolicies() {
        return discountPolicyRepository.findAllActivePolicies();
    }

}
