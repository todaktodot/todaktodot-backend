package com.todaktodot.TDTD.domain.login.service;

import com.todaktodot.TDTD.domain.login.dto.request.LoginRequestDTO;
import com.todaktodot.TDTD.domain.login.dto.request.TokenReissueRequestDTO;
import com.todaktodot.TDTD.domain.login.dto.response.LoginResponseDTO;
import com.todaktodot.TDTD.domain.login.dto.response.SocialUserResponse;
import com.todaktodot.TDTD.domain.login.dto.response.TokenReissueResponseDTO;
import com.todaktodot.TDTD.domain.login.respository.UserAccountRepository;
import com.todaktodot.TDTD.domain.login.respository.UserRepository;
import com.todaktodot.TDTD.domain.login.respository.entity.User;
import com.todaktodot.TDTD.domain.login.respository.entity.UserAccount;
import com.todaktodot.TDTD.domain.notification.repository.DeviceTokenRepository;
import com.todaktodot.TDTD.domain.notification.repository.entity.DeviceTokenEntity;
import com.todaktodot.TDTD.global.alert.DiscordNotificationService;
import com.todaktodot.TDTD.global.jwt.JwtTokenProvider;
import com.todaktodot.TDTD.global.security.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {
    private final SocialUserProvider socialUserProvider;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final UserAccountRepository userAccountRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final DiscordNotificationService discordNotificationService;

    /**
     * 로그인
     */
    @Override
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        // 1. 소셜 플랫폼에 토큰 검증 요청 및 유저 정보 추출
        SocialUserResponse socialUser = socialUserProvider.getLoginedSocialUser(loginRequestDTO.getProvider(), loginRequestDTO.getToken(), loginRequestDTO.getAppleName());

        // 2. 가입된 소셜계정인지 확인
        UserAccount userAccount = userAccountRepository.findByProviderIdAndProviderAndDelYn(socialUser.getId(), socialUser.getProvider(), "N")
                .orElseGet(() -> {
                    UserAccount newUserAccount = UserAccount.builder()
                            .email(socialUser.getEmail())
                            .name(socialUser.getName())
                            .kakaoNickname(socialUser.getKakaoNickname())
                            .appleRefreshToken(socialUser.getAppleRefreshToken())
                            .provider(socialUser.getProvider())
                            .providerId(socialUser.getId())
                            .build();

                    User newUser = User.builder()
                            .role(Role.USER)
                            .socialAccounts(new ArrayList<>(List.of(newUserAccount)))
                            .build();

                    newUserAccount.setUser(newUser);

                    User savedUser = userRepository.save(newUser);
                    UserAccount savedUserAccount = userAccountRepository.save(newUserAccount);

                    discordNotificationService.sendSuccessNotificationForNewUser(formatSignupMessage(savedUserAccount, savedUser.getId()));

                    return savedUserAccount;
                });

        // 3. 서비스 전용 JWT 토큰 발급
        User user = userAccount.getUser();
        if (user == null) throw new IllegalStateException("계정과 연결된 유저가 존재하지 않습니다.");

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        //4. 리프레쉬 토큰 저장
        userAccount.updateRefreshToken(refreshToken);
        userAccountRepository.save(userAccount);

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * 토큰 재발급
     */
    @Override
    @Transactional
    public TokenReissueResponseDTO reissue(TokenReissueRequestDTO tokenReissueRequestDTO) {
        String refreshToken = tokenReissueRequestDTO.getRefreshToken();

        if (!StringUtils.hasText(refreshToken)) {
            throw new IllegalStateException("Refresh Token이 존재하지 않습니다.");
        }

        User user = userRepository.findByIdAndDelYn(tokenReissueRequestDTO.getUserId(), "N")
                .orElseThrow(() -> new IllegalStateException("[userId : " +tokenReissueRequestDTO.getUserId()+" ]에 해당하는 유저가 없습니다."));

        validate(tokenReissueRequestDTO.getRefreshToken(), user);

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole());

        return new TokenReissueResponseDTO(accessToken, refreshToken);
    }

    /**
     * 로그아웃
     */
    @Override
    @Transactional
    public void logout(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("UserID는 필수입니다.");
        }

        UserAccount userAccount = userAccountRepository.findByUserIdAndDelYn(userId, "N")
                .orElseThrow(() -> new IllegalStateException("[UserID :" + userId + " ] 일치하는 계정이 없습니다."));

        //리프레쉬 초기화
        userAccount.updateRefreshToken(null);

        //디바이스 토큰 비활성화
        List<DeviceTokenEntity> activeTokensByUserId = deviceTokenRepository.findActiveTokensByUserId(userId);
        activeTokensByUserId.forEach(at -> {
            at.deactivate(userId);
        });
    }

    private void validate(String refreshToken, User user) {
        // 1. 유효한 리프레쉬인지
        jwtTokenProvider.validateToken(refreshToken);

        if (user.getSocialAccounts().isEmpty()) {
            throw new IllegalStateException("유저와 연결된 소셜 계정이 없습니다.");
        }

        // 2. 일치하는 리프레쉬토큰인지
        UserAccount userAccount = user.getSocialAccounts().getFirst();
        if (!userAccount.getRefreshToken().equals(refreshToken)) {
            throw new IllegalStateException("유효하지 않은 리프레쉬 토큰 입니다. 재로그인 해주세요.");
        }
    }

    private String formatSignupMessage(UserAccount userAccount, Long userId){
        String provider = userAccount.getProvider();
        String email = userAccount.getEmail();
        String name = userAccount.getName();

        return String.format(
            "**신규 가입자 발생**\n"
                    + "- **사용자 ID**: %s\n"
                    + "- **소셜 플랫폼**: %s\n"
                    + "- **이메일**: %s\n"
                    + "- **이름**: %s\n",
                userId != null ? userId : "ID 없음",
                StringUtils.hasText(provider) ? provider : "플랫폼 없음",
                StringUtils.hasText(email) ? email : "이메일 없음",
                StringUtils.hasText(name) ? name : "이름 없음"
        );
    }
}
