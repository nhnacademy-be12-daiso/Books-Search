package com.daisobook.shop.booksearch.books_search.service.point;

import com.daisobook.shop.booksearch.books_search.client.UserApiClient;
import com.daisobook.shop.booksearch.books_search.dto.point.PointPolicyType;
import com.daisobook.shop.booksearch.books_search.service.point.impl.PointServiceImpl;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PointServiceTest {

    @MockitoBean
    @Mock
    private UserApiClient userApiClient;

    @InjectMocks
    private PointServiceImpl pointService;

    private final long SAMPLE_USER_ID = 123L;

    @Test
    @DisplayName("requestReviewPoint: 포인트 적립 요청 성공 시 UserApiClient가 정확한 인수로 호출됨")
    void requestReviewPoint_success_invokesUserApiClient() {
        PointPolicyType policy = mock(PointPolicyType.class);
        when(policy.name()).thenReturn("TEST_POLICY");

        // 실행: 예외 없음
        assertDoesNotThrow(() -> pointService.requestReviewPoint(SAMPLE_USER_ID, policy));

        // 검증: 정확한 인수로 클라이언트 호출되었는지 확인
        verify(userApiClient, times(1)).earnPointByPolicy(SAMPLE_USER_ID, "TEST_POLICY");
    }

    @Test
    @DisplayName("requestReviewPoint: UserApiClient가 예외를 던질 때 예외가 전파됨")
    void requestReviewPoint_whenClientThrows_propagatesException() {
        PointPolicyType policy = mock(PointPolicyType.class);
        when(policy.name()).thenReturn("POL_FAIL");

        doThrow(new RuntimeException("client-fail"))
                .when(userApiClient).earnPointByPolicy(SAMPLE_USER_ID, "POL_FAIL");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> pointService.requestReviewPoint(SAMPLE_USER_ID, policy));
        assertEquals("client-fail", ex.getMessage());

        verify(userApiClient, times(1)).earnPointByPolicy(SAMPLE_USER_ID, "POL_FAIL");
    }

    @Test
    @DisplayName("recover: FeignException 처리 검증")
    void recover_canBeCalled_directly_handlesFeignException() {
        FeignException feignEx = mock(FeignException.class);
        PointPolicyType policy = mock(PointPolicyType.class);

        // recover는 void 반환, 외부 사이드 이펙트(로깅)를 수행하므로 호출만 검증
        assertDoesNotThrow(() -> pointService.recover(feignEx, SAMPLE_USER_ID, policy));
    }
}
