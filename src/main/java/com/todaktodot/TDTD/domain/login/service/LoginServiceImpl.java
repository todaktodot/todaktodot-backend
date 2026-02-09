package com.todaktodot.TDTD.domain.login.service;

import com.todaktodot.TDTD.domain.couple.repository.CoupleRepository;
import com.todaktodot.TDTD.domain.login.dto.request.LoginRequestDTO;
import com.todaktodot.TDTD.domain.login.dto.request.TokenReissueRequestDTO;
import com.todaktodot.TDTD.domain.login.dto.response.LoginResponseDTO;
import com.todaktodot.TDTD.domain.login.dto.response.SocialUserResponse;
import com.todaktodot.TDTD.domain.login.dto.response.TokenReissueResponseDTO;
import com.todaktodot.TDTD.domain.login.respository.UserAccountRepository;
import com.todaktodot.TDTD.domain.login.respository.UserRepository;
import com.todaktodot.TDTD.domain.login.respository.entity.User;
import com.todaktodot.TDTD.domain.login.respository.entity.UserAccount;
import com.todaktodot.TDTD.global.jwt.JwtTokenProvider;
import com.todaktodot.TDTD.global.security.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {
    private final SocialUserProvider socialUserProvider;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final UserAccountRepository userAccountRepository;
    private final CoupleRepository coupleRepository;

    /**
     * 로그인
     */
    @Override
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        // 1. 소셜 플랫폼에 토큰 검증 요청 및 유저 정보 추출
        SocialUserResponse socialUser = socialUserProvider.getLoginedSocialUser(loginRequestDTO.getProvider(), loginRequestDTO.getToken());

        // 2. 가입된 소셜계정인지 확인
        UserAccount userAccount = userAccountRepository.findByProviderIdAndProviderAndDelYn(socialUser.getId(), socialUser.getProvider(), "N")
                .orElseGet(() -> {
                    UserAccount newUserAccount = UserAccount.builder()
                        .email(socialUser.getEmail())
                        .name(socialUser.getName())
                        .provider(socialUser.getProvider())
                        .providerId(socialUser.getId())
                        .build();

                    User newUser = User.builder()
                            .alarmYN("Y")
                            .joinYN("N")
                            .role(Role.USER)
                            .socialAccounts(new ArrayList<>(List.of(newUserAccount)))
                            .build();

                    newUserAccount.setUser(newUser);

                    userRepository.save(newUser);
                    return userAccountRepository.save(newUserAccount);
                });

        // 3. 서비스 전용 JWT 토큰 발급
        User user = userAccount.getUser();
        if (user == null) throw new IllegalStateException("계정과 연결된 유저가 존재하지 않습니다.");

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        //4. 리프레쉬 토큰 저장
        userAccount.updateRefreshToken(refreshToken);
        userAccountRepository.save(userAccount);

        //4. 커플여부 확인 [TDTDBE-55] coupleType 추가
        var coupleOpt = coupleRepository.findByUserId(user.getId());
        boolean isCouple = coupleOpt.map(c -> c.isComplete()).orElse(false);
        String coupleType = coupleOpt.map(c -> c.getCoupleType().name()).orElse(null);

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .isJoined(user.getJoinYN().equals("Y"))
                .isCouple(isCouple)
                .coupleType(coupleType)
                .build();
    }

    /**
     * 토큰 재발급
     */
    @Override
    public TokenReissueResponseDTO reissue(TokenReissueRequestDTO tokenReissueRequestDTO) {
        String refreshToken = tokenReissueRequestDTO.getRefreshToken();
        if (refreshToken == null) throw new IllegalStateException("Refresh Token이 존재하지 않습니다.");
        User user = userRepository.findById(tokenReissueRequestDTO.getUserId())
                .orElseThrow(() -> new IllegalStateException("[userId : " +tokenReissueRequestDTO.getUserId()+" ]에 해당하는 유저가 없습니다."));
        validate(tokenReissueRequestDTO.getRefreshToken(), user);

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole());

        return new TokenReissueResponseDTO(accessToken, refreshToken);
    }

    private void validate(String refreshToken, User user) {
        // 1. 유효한 리프레쉬인지
        jwtTokenProvider.validateToken(refreshToken);
        if (user.getSocialAccounts().isEmpty()) throw new IllegalStateException("유저와 연결된 소셜 계정이 없습니다.");

        // 2. 일치하는 리프레쉬토큰인지
        UserAccount userAccount = user.getSocialAccounts().getFirst();
        if (!userAccount.getRefreshToken().equals(refreshToken)) {
            throw new IllegalStateException("유효하지 않은 리프레쉬 토큰 입니다. 재로그인 해주세요.");
        }
    }
}
