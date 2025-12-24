package com.todaktodot.TDTD.couplelink.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaktodot.TDTD.domain.couplelink.controller.CoupleLinkAuthController;
import com.todaktodot.TDTD.domain.couplelink.dto.response.IssueLinkCodeResponseDTO;
import com.todaktodot.TDTD.domain.couplelink.service.CoupleLinkAuthService;
import com.todaktodot.TDTD.domain.login.respository.entity.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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

    private UserPrincipal createTestUserPrincipal(Long userId) {
        return new UserPrincipal(userId, Collections.emptyList());
    }

    @Test
    @DisplayName("POST /api/couple-link/issue - 코드 발급 성공")
    void issueLinkCode_Success() throws Exception {
        // Given
        IssueLinkCodeResponseDTO responseDTO = IssueLinkCodeResponseDTO.builder()
                .linkCode("ABC123")
                .expiredDt(LocalDateTime.now().plusMinutes(30))
                .build();

        when(coupleLinkAuthService.issueLinkCode(anyLong()))
                .thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(post("/api/couple-link/issue")
                        .with(csrf())
                        .with(user(createTestUserPrincipal(1L)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.linkCode").value("ABC123"))
                .andExpect(jsonPath("$.expiredDt").exists());
    }

    @Test
    @DisplayName("POST /api/couple-link/issue - 인증 없이 요청 시 401")
    void issueLinkCode_Unauthorized_WithoutAuth() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/couple-link/issue")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}
