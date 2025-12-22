package com.todaktodot.TDTD.domain.login.controller;

import com.todaktodot.TDTD.domain.login.dto.response.LoginTokenResponseDTO;
import com.todaktodot.TDTD.domain.login.respository.UserRepository;
import com.todaktodot.TDTD.domain.login.respository.entity.User;
import com.todaktodot.TDTD.global.jwt.JwtTokenProvider;
import com.todaktodot.TDTD.global.security.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/login")
//@Profile("local")
@Tag(name = "TestLoginController", description = "테스트용 로그인 API (local 환경 전용)")
public class TestLoginController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(description = "테스트 로그인 - 테스트 유저로 JWT 토큰 발급 (local 환경 전용)")
    @ApiResponse(responseCode = "200", description = "테스트 로그인 성공")
    @PostMapping("/test1")
    public ResponseEntity<LoginTokenResponseDTO> testLogin1() {
        // 테스트 유저 조회 또는 생성
        User testUser = userRepository.findByProviderIdAndProvider("test123", "test")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("test1@todaktodak.com")
                        .name("김투닥")
                        .provider("test")
                        .providerId("test123")
                        .role(Role.USER)
                        .build()));

        // JWT 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(testUser.getId(), testUser.getEmail(), testUser.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(testUser.getId());

        return ResponseEntity.ok(new LoginTokenResponseDTO(accessToken, refreshToken));
    }

    @Operation(description = "테스트 로그인 - 테스트 유저로 JWT 토큰 발급 (local 환경 전용)")
    @ApiResponse(responseCode = "200", description = "테스트 로그인 성공")
    @PostMapping("/test2")
    public ResponseEntity<LoginTokenResponseDTO> testLogin2() {
        // 테스트 유저 조회 또는 생성
        User testUser = userRepository.findByProviderIdAndProvider("test456", "test")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("test2@todaktodak.com")
                        .name("이투닷")
                        .provider("test")
                        .providerId("test456")
                        .role(Role.USER)
                        .build()));

        // JWT 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(testUser.getId(), testUser.getEmail(), testUser.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(testUser.getId());

        return ResponseEntity.ok(new LoginTokenResponseDTO(accessToken, refreshToken));
    }
}