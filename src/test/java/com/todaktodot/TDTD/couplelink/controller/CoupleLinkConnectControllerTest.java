package com.todaktodot.TDTD.couplelink.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaktodot.TDTD.domain.couplelink.controller.CoupleLinkAuthController;
import com.todaktodot.TDTD.domain.couplelink.dto.request.ConnectLinkCodeRequestDTO;
import com.todaktodot.TDTD.domain.couplelink.dto.response.ConnectLinkCodeResponseDTO;
import com.todaktodot.TDTD.domain.couplelink.service.CoupleLinkAuthService;
import com.todaktodot.TDTD.domain.login.respository.entity.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@WebMvcTest(CoupleLinkAuthController.class)
@DisplayName("커플 연결 컨트롤러 테스트")
class CoupleLinkConnectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CoupleLinkAuthService coupleLinkAuthService;

    private UserPrincipal createTestUserPrincipal(Long userId) {
        return new UserPrincipal(userId, Collections.emptyList());
    }

    @Test
    @DisplayName("POST /api/couple-link/connect - 커플 연결 성공")
    void connectLinkCode_Success() throws Exception {
        // Given
        ConnectLinkCodeRequestDTO requestDTO = new ConnectLinkCodeRequestDTO("ABC123");

        ConnectLinkCodeResponseDTO responseDTO = ConnectLinkCodeResponseDTO.builder()
                .coupleId(1L)
                .userId1(1L)
                .userId2(2L)
                .connectedDt(LocalDateTime.now())
                .build();

        when(coupleLinkAuthService.connectLinkCode(anyLong(), any(ConnectLinkCodeRequestDTO.class)))
                .thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(post("/api/couple-link/connect")
                        .with(csrf())
                        .with(user(createTestUserPrincipal(2L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.coupleId").value(1))
                .andExpect(jsonPath("$.userId1").value(1))
                .andExpect(jsonPath("$.userId2").value(2))
                .andExpect(jsonPath("$.connectedDt").exists());
    }

    @Test
    @DisplayName("POST /api/couple-link/connect - 인증 없이 요청 시 401")
    void connectLinkCode_Unauthorized_WithoutAuth() throws Exception {
        // Given
        ConnectLinkCodeRequestDTO requestDTO = new ConnectLinkCodeRequestDTO("ABC123");

        // When & Then
        mockMvc.perform(post("/api/couple-link/connect")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",                                // linkCode 없음
            "{\"linkCode\": \"\"}",              // linkCode 빈 문자열
            "{\"linkCode\": \"AB1\"}",           // linkCode 형식 오류 (3자리)
            "{\"linkCode\": \"abc123\"}"         // linkCode 형식 오류 (소문자)
    })
    @DisplayName("POST /api/couple-link/connect - Validation 실패")
    void connectLinkCode_ValidationFailed(String requestJson) throws Exception {
        // When & Then
        mockMvc.perform(post("/api/couple-link/connect")
                        .with(csrf())
                        .with(user(createTestUserPrincipal(2L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    assertThat(result.getResolvedException())
                            .isInstanceOf(MethodArgumentNotValidException.class);

                    MethodArgumentNotValidException exception =
                            (MethodArgumentNotValidException) result.getResolvedException();

                    log.info("========================================");
                    log.info("요청 JSON: {}", requestJson);
                    log.info("검증 에러 개수: {}", exception.getBindingResult().getErrorCount());
                    exception.getBindingResult().getFieldErrors().forEach(error ->
                            log.info("필드: {}, 메시지: {}", error.getField(), error.getDefaultMessage())
                    );
                    log.info("========================================");
                });
    }
}
