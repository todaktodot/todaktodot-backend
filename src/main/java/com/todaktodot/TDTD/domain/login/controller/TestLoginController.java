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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
        return ResponseEntity.ok(loginWithTestUser("김투닥", "test123", "test"));
    }

    @Operation(summary = "테스트 로그인 - 이투닷", description = "local, dev 전용, 테스트 용도 회원 데이터(이투닷)로 토큰 발급합니다.")
    @ApiResponse(responseCode = "200", description = "테스트 로그인 성공")
    @PostMapping("/test2")
    public ResponseEntity<LoginResponseDTO> testLogin2() {
        return ResponseEntity.ok(loginWithTestUser("이투닷", "test456", "test"));
    }

    @Operation(summary = "테스트 로그인 - 커스텀", description = "local, dev 전용, 파라미터를 지정하여 테스트 계정 생성 및 토큰 발급합니다. 같은 providerId+provider로 재호출 시 기존 계정의 토큰을 반환합니다. 호출 전에 반드시 서버 개발자에게 검토를 받으시는걸 추천드립니다.")
    @ApiResponse(responseCode = "200", description = "테스트 로그인 성공")
    @PostMapping("/test")
    public ResponseEntity<LoginResponseDTO> testLoginCustom(
            @Parameter(description = "이름", example = "김테스트")
            @RequestParam String name,
            @Parameter(description = "고유 식별자", example = "test10001")
            @RequestParam String providerId,
            @Parameter(description = "인증 제공자", example = "KAKAO, GOOGLE, APPLE(예정)")
            @RequestParam(defaultValue = "test") String provider) {

        return ResponseEntity.ok(loginWithTestUser(name, providerId, provider));
    }

    private UserAccount getOrCreateTestUser(String name, String providerId, String provider) {

        UserAccount userTestAccount = userAccountRepository.findByProviderIdAndProviderAndDelYn(providerId, provider, "N")
                .orElseGet(() -> {
                    UserAccount newUserAccount = UserAccount.builder()
                            .email(providerId + "@todaktodak.com")
                            .name(name)
                            .provider(provider)
                            .providerId(providerId)
                            .build();

                    User newUser = User.builder()
                            .role(Role.USER)
                            .socialAccounts(new ArrayList<>(List.of(newUserAccount)))
                            .build();

                    newUserAccount.setUser(newUser);

                    userRepository.save(newUser);
                    return userAccountRepository.save(newUserAccount);
                });
        return userTestAccount;
    }

    private LoginResponseDTO loginWithTestUser(String name, String providerId, String provider) {
        UserAccount userTestAccount = getOrCreateTestUser(name, providerId, provider);

        User testUser = userTestAccount.getUser();
        // [TDTDBE-55] coupleType 추가
        var coupleOpt = coupleRepository.findByUserId(testUser.getId());
        boolean isCouple = coupleOpt.map(c -> c.isComplete()).orElse(false);
        String coupleType = coupleOpt.map(c -> c.getCoupleType().name()).orElse(null);

        String accessToken = jwtTokenProvider.createAccessToken(testUser.getId(), testUser.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(testUser.getId());

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .isJoined(testUser.getTermYN().equals("Y"))
                .isCouple(isCouple)
                .coupleType(coupleType)
                .build();
    }
}
