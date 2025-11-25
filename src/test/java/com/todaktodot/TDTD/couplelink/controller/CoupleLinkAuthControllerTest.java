package com.todaktodot.TDTD.couplelink.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaktodot.TDTD.couplelink.dto.request.IssueLinkCodeRequestDTO;
import com.todaktodot.TDTD.couplelink.dto.response.IssueLinkCodeResponseDTO;
import com.todaktodot.TDTD.couplelink.service.CoupleLinkAuthService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@WebMvcTest(CoupleLinkAuthController.class)
@DisplayName("커플 연결 코드 발급 컨트롤러 테스트")
class CoupleLinkAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CoupleLinkAuthService coupleLinkAuthService;

    @Test
    @WithMockUser
    @DisplayName("POST /api/couple-link/issue - 코드 발급 성공")
    void issueLinkCode_Success() throws Exception {
        // Given
        IssueLinkCodeRequestDTO requestDTO = new IssueLinkCodeRequestDTO();
        requestDTO.setUserId("testUser123");

        IssueLinkCodeResponseDTO responseDTO = IssueLinkCodeResponseDTO.builder()
                .linkCode("ABC123")
                .expiredDt(LocalDateTime.now().plusMinutes(30))
                .build();

        when(coupleLinkAuthService.issueLinkCode(any(IssueLinkCodeRequestDTO.class)))
                .thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(post("/api/couple-link/issue")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.linkCode").value("ABC123"))
                .andExpect(jsonPath("$.expiryMinutes").value(30))
                .andExpect(jsonPath("$.expiredDt").exists());
    }

    @Test
    @DisplayName("POST /api/couple-link/issue - 인증 없이 요청 시 401 (Spring Security) - 추가 후 검증")
    void issueLinkCode_Unauthorized_WithoutAuth() throws Exception {
        // Given
        IssueLinkCodeRequestDTO requestDTO = new IssueLinkCodeRequestDTO();

        // When & Then
        mockMvc.perform(post("/api/couple-link/issue")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @WithMockUser
    @ValueSource(strings = {
            "{}",                      // userId가 null (필드 없음)
            "{\"userId\": \"\"}",      // userId가 빈 문자열
            "{\"userId\": \"   \"}"    // userId가 공백만
    })
    @DisplayName("POST /api/couple-link/issue - userId가 유효하지 않을 때 400 에러 및 에러 메시지 확인")
    void issueLinkCode_BadRequest_WhenUserIdIsInvalid(String requestJson) throws Exception {
        // When & Then
        mockMvc.perform(post("/api/couple-link/issue")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    // 예외가 MethodArgumentNotValidException인지 확인
                    assertThat(result.getResolvedException())
                            .isInstanceOf(MethodArgumentNotValidException.class);

                    // 예외에서 검증 에러 메시지 추출
                    MethodArgumentNotValidException exception =
                            (MethodArgumentNotValidException) result.getResolvedException();

                    String errorMessage = Objects.requireNonNull(
                            exception.getBindingResult()
                                    .getFieldError("userId"))
                            .getDefaultMessage();

                    // 로그로 에러 메시지 출력 (콘솔에서 확인 가능)
                    log.info("========================================");
                    log.info("검증 에러 메시지: {}", errorMessage);
                    log.info("요청 JSON: {}", requestJson);
                    log.info("========================================");

                    // "사용자 ID는 필수입니다" 메시지 확인
                    assertThat(errorMessage).isEqualTo("사용자 ID는 필수입니다");
                });
    }
}