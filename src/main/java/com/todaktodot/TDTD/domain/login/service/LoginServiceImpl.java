package com.todaktodot.TDTD.domain.login.service;

import com.todaktodot.TDTD.domain.login.dto.request.LoginRequestDTO;
import com.todaktodot.TDTD.domain.login.dto.response.LoginTokenResponseDTO;
import com.todaktodot.TDTD.domain.login.dto.response.SocialUserResponse;
import com.todaktodot.TDTD.domain.login.respository.UserRepository;
import com.todaktodot.TDTD.domain.login.respository.entity.User;
import com.todaktodot.TDTD.global.jwt.JwtTokenProvider;
import com.todaktodot.TDTD.global.security.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {
    private final SocialUserProvider socialUserProvider;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    /**
     * 로그인
     */
    @Override
    public LoginTokenResponseDTO login(LoginRequestDTO loginRequestDTO) {
        // 1. 소셜 플랫폼에 토큰 검증 요청 및 유저 정보 추출
        SocialUserResponse socialUser = socialUserProvider.getLoginedSocialUser(loginRequestDTO.getProvider(), loginRequestDTO.getToken());

        // 2. DB 저장 혹은 조회
        User user = userRepository.findByProviderIdAndProvider(socialUser.getId(), socialUser.getProvider())
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(socialUser.getEmail())
                        .name(socialUser.getName())
                        .provider(socialUser.getProvider())
                        .providerId(socialUser.getId())
                        .role(Role.USER)
                        .build()));

        // 3. 서비스 전용 JWT 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        return new LoginTokenResponseDTO(accessToken, refreshToken);
    }
}
