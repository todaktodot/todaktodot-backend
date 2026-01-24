package com.todaktodot.TDTD.domain.login.service;

import com.todaktodot.TDTD.domain.couple.repository.CoupleRepository;
import com.todaktodot.TDTD.domain.login.dto.request.LoginRequestDTO;
import com.todaktodot.TDTD.domain.login.dto.response.LoginResponseDTO;
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
    private final CoupleRepository coupleRepository;

    /**
     * 로그인
     */
    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        // 1. 소셜 플랫폼에 토큰 검증 요청 및 유저 정보 추출
        SocialUserResponse socialUser = socialUserProvider.getLoginedSocialUser(loginRequestDTO.getProvider(), loginRequestDTO.getToken());

        // 2. DB 저장 혹은 조회
        User user = userRepository.findByProviderIdAndProvider(socialUser.getId(), socialUser.getProvider())
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(socialUser.getEmail())
                        .name(socialUser.getName())
                        .provider(socialUser.getProvider())
                        .providerId(socialUser.getId())
                        .alarmYN("Y")
                        .joinYN("N")
                        .role(Role.USER)
                        .build()));

        // 3. 서비스 전용 JWT 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        //4. 커플여부 확인
        boolean isCouple = coupleRepository.existsByUserId(user.getId());

        return new LoginResponseDTO(accessToken, refreshToken, user.getJoinYN().equals("Y"), isCouple);
    }
}
