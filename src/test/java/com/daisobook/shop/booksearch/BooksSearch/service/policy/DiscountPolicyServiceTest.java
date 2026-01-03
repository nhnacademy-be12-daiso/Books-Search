package com.daisobook.shop.booksearch.BooksSearch.service.policy;

import com.daisobook.shop.booksearch.BooksSearch.dto.DiscountDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.DiscountValueListData;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.DiscountValueListProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.DiscountValueProjection;
import com.daisobook.shop.booksearch.BooksSearch.entity.policy.DiscountPolicy;
import com.daisobook.shop.booksearch.BooksSearch.entity.policy.DiscountType;
import com.daisobook.shop.booksearch.BooksSearch.repository.policy.DiscountPolicyRepository;
import com.daisobook.shop.booksearch.BooksSearch.service.policy.impl.DiscountPolicyServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DiscountPolicyServiceTest {

    @MockitoBean
    @Mock
    private DiscountPolicyRepository discountPolicyRepository;

    @MockitoBean @Mock
    private ObjectMapper objectMapper;

    @Spy
    @InjectMocks
    private DiscountPolicyServiceImpl discountPolicyService;

    @Test
    @DisplayName("getDiscountValueByBookIdOrCategoryIdsOrPublisherId: 모든 소스에서 할인 정책을 가져옴")
    void getDiscountValueByBookIdOrCategoryIdsOrPublisherId_combinesAllSources() {
        DiscountValueProjection global = mock(DiscountValueProjection.class);
        DiscountValueProjection category = mock(DiscountValueProjection.class);
        DiscountValueProjection publisher = mock(DiscountValueProjection.class);
        DiscountValueProjection product = mock(DiscountValueProjection.class);

        when(discountPolicyRepository.findAllByTargetTypeAndTargetIdAndIsActive(org.mockito.ArgumentMatchers.eq(com.daisobook.shop.booksearch.BooksSearch.entity.policy.TargetType.GLOBAL), isNull(), eq(true)))
                .thenReturn(List.of(global));
        when(discountPolicyRepository.findAllByTargetTypeAndTargetIdInAndIsActive(org.mockito.ArgumentMatchers.eq(com.daisobook.shop.booksearch.BooksSearch.entity.policy.TargetType.CATEGORY), anyList(), eq(true)))
                .thenReturn(List.of(category));
        when(discountPolicyRepository.findAllByTargetTypeAndTargetIdAndIsActive(org.mockito.ArgumentMatchers.eq(com.daisobook.shop.booksearch.BooksSearch.entity.policy.TargetType.PUBLISHER), eq(10L), eq(true)))
                .thenReturn(List.of(publisher));
        when(discountPolicyRepository.findAllByTargetTypeAndTargetIdAndIsActive(org.mockito.ArgumentMatchers.eq(com.daisobook.shop.booksearch.BooksSearch.entity.policy.TargetType.PRODUCT), eq(99L), eq(true)))
                .thenReturn(List.of(product));

        List<DiscountValueProjection> result = discountPolicyService.getDiscountValueByBookIdOrCategoryIdsOrPublisherId(List.of(1L,2L), 10L, 99L);

        assertNotNull(result);
        assertEquals(4, result.size());
        verify(discountPolicyRepository, times(1)).findAllByTargetTypeAndTargetIdAndIsActive(org.mockito.ArgumentMatchers.eq(com.daisobook.shop.booksearch.BooksSearch.entity.policy.TargetType.GLOBAL), isNull(), eq(true));
        verify(discountPolicyRepository, times(1)).findAllByTargetTypeAndTargetIdInAndIsActive(org.mockito.ArgumentMatchers.eq(com.daisobook.shop.booksearch.BooksSearch.entity.policy.TargetType.CATEGORY), anyList(), eq(true));
        verify(discountPolicyRepository, times(1)).findAllByTargetTypeAndTargetIdAndIsActive(org.mockito.ArgumentMatchers.eq(com.daisobook.shop.booksearch.BooksSearch.entity.policy.TargetType.PUBLISHER), eq(10L), eq(true));
        verify(discountPolicyRepository, times(1)).findAllByTargetTypeAndTargetIdAndIsActive(org.mockito.ArgumentMatchers.eq(com.daisobook.shop.booksearch.BooksSearch.entity.policy.TargetType.PRODUCT), eq(99L), eq(true));
    }

    @Test
    @DisplayName("getDiscountPolicyByData: JSON 파싱 후 결과 반환")
    void getDiscountPolicyByData_parsesJsonAndSkipsNullProjection() throws Exception {
        Long bookId = 5L;
        DiscountValueListProjection dv1 = mock(DiscountValueListProjection.class);
        DiscountValueListProjection dvNull = null;
        when(dv1.getDiscountValueList()).thenReturn("json1");

        when(discountPolicyRepository.getDiscountValue(new ArrayList<>(Collections.singleton(bookId))))
                .thenReturn(Arrays.asList(dvNull, dv1));

        @SuppressWarnings("unchecked")
        List<DiscountValueListData> parsed = List.of(mock(DiscountValueListData.class));
        when(objectMapper.readValue(eq("json1"), any(TypeReference.class))).thenReturn(parsed);

        List<DiscountValueListData> res = discountPolicyService.getDiscountPolicyByData(bookId);
        assertNotNull(res);
        assertEquals(1, res.size());
        verify(objectMapper, times(1)).readValue(eq("json1"), any(TypeReference.class));
    }

    @Test
    @DisplayName("getDiscountPolicyByData: JSON 파싱 실패 시 예외 발생")
    void getDiscountPolicyByData_whenJsonProcessingFails_throws() throws Exception {
        Long bookId = 6L;
        DiscountValueListProjection dv = mock(DiscountValueListProjection.class);
        when(dv.getDiscountValueList()).thenReturn("bad-json");
        when(discountPolicyRepository.getDiscountValue(new ArrayList<>(Collections.singleton(bookId))))
                .thenReturn(List.of(dv));

        when(objectMapper.readValue(eq("bad-json"), ArgumentMatchers.any(TypeReference.class)))
                .thenThrow(new JsonProcessingException("parse fail") {});

        assertThrows(JsonProcessingException.class, () -> discountPolicyService.getDiscountPolicyByData(bookId));
        verify(objectMapper, times(1)).readValue(eq("bad-json"), ArgumentMatchers.any(TypeReference.class));
    }

    @Test
    @DisplayName("getDiscountPrice: 퍼센트 및 고정 금액 할인 적용")
    void getDiscountPrice_appliesPercentageAndFixedAmountCorrectly() throws Exception {
        Long bookId = 7L;
        DiscountValueListData p = mock(DiscountValueListData.class);
        when(p.getDiscountType()).thenReturn(DiscountType.PERCENTAGE);
        when(p.getValue()).thenReturn((double) 10L); // 10%

        DiscountValueListData f = mock(DiscountValueListData.class);
        when(f.getDiscountType()).thenReturn(DiscountType.FIXED_AMOUNT);
        when(f.getValue()).thenReturn((double) 500L); // 500원

        List<DiscountValueListData> list = List.of(p, f);
        doReturn(list).when(discountPolicyService).getDiscountPolicyByData(bookId);

        Long originalPrice = 5000L;
        Long discounted = discountPolicyService.getDiscountPrice(bookId, originalPrice);

        assertEquals(4000L, discounted);
    }
    @Test
    @DisplayName("getDiscountPrice: 가격이 null일 때 null 반환")
    void getDiscountPrice_withNullPrice_returnsNull() throws Exception {
        long bookId = 8L;
        Long res = discountPolicyService.getDiscountPrice(bookId, null);
        assertNull(res);
    }

    @Test
    @DisplayName("getDiscountPolicyByDataMap: 도서별로 맵을 생성함")
    void getDiscountPolicyByDataMap_buildsMapPerBook() throws Exception {
        DiscountValueListProjection dv1 = mock(DiscountValueListProjection.class);
        DiscountValueListProjection dv2 = mock(DiscountValueListProjection.class);
        when(dv1.getBookId()).thenReturn(1L);
        when(dv1.getDiscountValueList()).thenReturn("j1");
        when(dv2.getBookId()).thenReturn(2L);
        when(dv2.getDiscountValueList()).thenReturn("j2");

        List<DiscountValueListProjection> repoReturn = List.of(dv1, dv2);
        List<Long> bookIds = List.of(1L,2L);
        when(discountPolicyRepository.getDiscountValue(bookIds)).thenReturn(repoReturn);

        @SuppressWarnings("unchecked")
        List<DiscountValueListData> parsed1 = List.of(mock(DiscountValueListData.class));
        @SuppressWarnings("unchecked")
        List<DiscountValueListData> parsed2 = List.of(mock(DiscountValueListData.class));
        when(objectMapper.readValue(eq("j1"), ArgumentMatchers.any(TypeReference.class))).thenReturn(parsed1);
        when(objectMapper.readValue(eq("j2"), ArgumentMatchers.any(TypeReference.class))).thenReturn(parsed2);

        Map<Long, List<DiscountValueListData>> map = discountPolicyService.getDiscountPolicyByDataMap(bookIds);
        assertNotNull(map);
        assertTrue(map.containsKey(1L));
        assertTrue(map.containsKey(2L));
        assertEquals(parsed1, map.get(1L));
        assertEquals(parsed2, map.get(2L));
    }

    @Test
    @DisplayName("getDiscountPriceMap: 요청 맵을 처리하고 누락되거나 잘못된 항목을 건너뜀")
    void getDiscountPriceMap_computesResponsesAndSkipsMissingOrInvalid() throws Exception {
        // prepare input request map
        DiscountDTO.Request req1 = mock(DiscountDTO.Request.class);
        when(req1.bookId()).thenReturn(100L);
        when(req1.price()).thenReturn(2000L);

        DiscountDTO.Request req2 = mock(DiscountDTO.Request.class);
        when(req2.bookId()).thenReturn(200L);
        when(req2.price()).thenReturn(null); // will trigger warning / skip

        Map<Long, DiscountDTO.Request> reqMap = new HashMap<>();
        reqMap.put(100L, req1);
        reqMap.put(200L, req2);

        // stub internal getDiscountPolicyByDataMap via spy
        DiscountValueListData p = mock(DiscountValueListData.class);
        when(p.getDiscountType()).thenReturn(DiscountType.PERCENTAGE);
        when(p.getValue()).thenReturn((double) 50L); // 50% off

        Map<Long, List<DiscountValueListData>> policyMap = new HashMap<>();
        policyMap.put(100L, List.of(p));
        // 200L intentionally absent or empty to test skipping behavior

        doReturn(policyMap).when(discountPolicyService).getDiscountPolicyByDataMap(anyList());

        Map<Long, DiscountDTO.Response> res = discountPolicyService.getDiscountPriceMap(reqMap);
        assertNotNull(res);
        assertTrue(res.containsKey(100L));
        DiscountDTO.Response r = res.get(100L);
        assertNotNull(r);
        // 원가 2000, 50% -> 1000 할인 가격 (구체 수치 검증)
        assertEquals(1000L, r.discountPrice());
    }

    @Test
    @DisplayName("getAllActiveDiscountPolicies: 활성화된 모든 할인 정책을 조회함")
    void getAllActiveDiscountPolicies_delegatesToRepository() {
        DiscountPolicy dp = mock(DiscountPolicy.class);
        when(discountPolicyRepository.findAllActivePolicies()).thenReturn(List.of(dp));

        List<DiscountPolicy> res = discountPolicyService.getAllActiveDiscountPolicies();
        assertNotNull(res);
        assertEquals(1, res.size());
        verify(discountPolicyRepository, times(1)).findAllActivePolicies();
    }
}
