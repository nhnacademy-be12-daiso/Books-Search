package com.daisobook.shop.booksearch.handler;

import com.daisobook.shop.booksearch.exception.CustomExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {TestController.class})
@Import(CustomExceptionHandler.class)
public class CustomExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("404: EntityNotFoundException 발생 시 404와 메시지를 반환한다")
    void handleNotFoundTest() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.body.detail").value("대상 없음"))
                .andExpect(jsonPath("$.body.status").value(404));
    }

    @Test
    @DisplayName("409: DuplicateResourceException 발생 시 409와 메시지를 반환한다")
    void handleConflictTest() throws Exception {
        mockMvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.body.detail").value("중복 발생"))
                .andExpect(jsonPath("$.body.status").value(409));
    }

    @Test
    @DisplayName("400: InvalidRequestException 발생 시 400과 메시지를 반환한다")
    void handleBadRequestTest() throws Exception {
        mockMvc.perform(get("/test/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.body.detail").value("잘못된 요청"))
                .andExpect(jsonPath("$.body.status").value(400));
    }

    @Test
    @DisplayName("500: 시스템 예외 발생 시 보안을 위해 고정된 메시지를 반환한다")
    void handleInternalServerErrorTest() throws Exception {
        mockMvc.perform(get("/test/internal-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.body.detail").value("시스템 내부 오류가 발생했습니다."))
                .andExpect(jsonPath("$.body.status").value(500));
    }

    @Test
    @DisplayName("500: 처리되지 않은 런타임 예외 발생 시 500을 반환한다")
    void handleAllTest() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.body.detail").value("알 수 없는 오류가 발생했습니다."))
                .andExpect(jsonPath("$.body.status").value(500));
    }
}