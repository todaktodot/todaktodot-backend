package com.todaktodot.TDTD.domain.login.controller;

import com.todaktodot.TDTD.domain.couple.repository.CoupleRepository;
import com.todaktodot.TDTD.domain.login.dto.response.LoginResponseDTO;
import com.todaktodot.TDTD.domain.login.respository.UserAccountRepository;
import com.todaktodot.TDTD.domain.login.respository.UserRepository;
import com.todaktodot.TDTD.domain.login.respository.entity.User;
import com.todaktodot.TDTD.domain.login.respository.entity.UserAccount;
import com.todaktodot.TDTD.global.jwt.JwtTokenProvider;
import com.todaktodot.TDTD.global.security.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/login")
//@Profile("local")
@Tag(name = "테스트용 로그인", description = "테스트용 로그인 API (local, dev 환경 전용)")
public class TestLoginController {

    private final UserAccountRepository userAccountRepository;
    private final UserRepository userRepository;
    private final CoupleRepository coupleRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "테스트 로그인 - 김투닥", description = "local, dev 전용, 테스트 용도 회원 데이터(김투닥)로 토큰 발급합니다.")
    @ApiResponse(responseCode = "200", description = "테스트 로그인 성공")
    @PostMapping("/test1")
    public ResponseEntity<LoginResponseDTO> testLogin1() {
        // 테스트 유저 조회 또는 생성
        UserAccount userTestAccount = userAccountRepository.findByProviderIdAndProviderAndDelYn("test123", "test", "N")
                .orElseGet(() -> {
                    UserAccount newUserAccount = UserAccount.builder()
                            .email("test1@todaktodak.com")
                            .name("김투닥")
                            .provider("test")
                            .providerId("test123")
                            .build();

                    User newUser = User.builder()
                            .nickname("김투닥")
                            .alarmYN("Y")
                            .joinYN("N")
                            .role(Role.USER)
                            .socialAccounts(new ArrayList<>(List.of(newUserAccount)))
                            .build();

                    newUserAccount.setUser(newUser);

                    userRepository.save(newUser);
                    return userAccountRepository.save(newUserAccount);
                });

        User testUser = userTestAccount.getUser();
        boolean isCouple = coupleRepository.existsByUserId(testUser.getId());

        // JWT 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(testUser.getId(), testUser.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(testUser.getId());

        return ResponseEntity.ok(new LoginResponseDTO(accessToken, refreshToken, testUser.getJoinYN().equals("Y"), isCouple));
    }

    @Operation(summary = "테스트 로그인 - 이투닷", description = "local, dev 전용, 테스트 용도 회원 데이터(이투닷)로 토큰 발급합니다.")
    @ApiResponse(responseCode = "200", description = "테스트 로그인 성공")
    @PostMapping("/test2")
    public ResponseEntity<LoginResponseDTO> testLogin2() {
        // 테스트 유저 조회 또는 생성
        UserAccount userTestAccount = userAccountRepository.findByProviderIdAndProviderAndDelYn("test456", "test", "N")
                .orElseGet(() -> {
                    UserAccount newUserAccount = UserAccount.builder()
                            .email("test2@todaktodak.com")
                            .name("이투닷")
                            .provider("test")
                            .providerId("test456")
                            .build();

                    User newUser = User.builder()
                            .nickname("김투닥")
                            .alarmYN("Y")
                            .joinYN("N")
                            .role(Role.USER)
                            .socialAccounts(new ArrayList<>(List.of(newUserAccount)))
                            .build();

                    newUserAccount.setUser(newUser);

                    userRepository.save(newUser);
                    return userAccountRepository.save(newUserAccount);
                });

        User testUser = userTestAccount.getUser();
        boolean isCouple = coupleRepository.existsByUserId(testUser.getId());

        // JWT 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(testUser.getId(), testUser.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(testUser.getId());

        return ResponseEntity.ok(new LoginResponseDTO(accessToken, refreshToken, testUser.getJoinYN().equals("Y"), isCouple));
    }
}