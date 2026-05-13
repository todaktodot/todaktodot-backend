package com.todaktodot.TDTD.domain.login.service.client;

import com.todaktodot.TDTD.domain.login.dto.response.SocialUserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoClient {

    @Value("${oauth.kakao.admin-key}")
    private String adminKey;

    private final WebClient webClient = WebClient.create();

    public SocialUserResponse getUserInfo(String accessToken) {
        Map<String, Object> response = webClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        //TODO : 예외처리
        if (response == null || response.get("id") == null) {
            throw new RuntimeException();
        }

        //
        Map<String, Object> kakaoAccount = (Map<String, Object>) response.get("kakao_account");

        String nickname = null;
        if (kakaoAccount != null && kakaoAccount.get("profile") != null) {
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            nickname = (String) profile.get("nickname");
        }

        return SocialUserResponse.builder()
                .id(response.get("id").toString())
                .kakaoNickname(nickname)
                .provider("KAKAO")
                        .build();
    }

    /**
     * 계정 연결 해제
     */
    public void revokeUser(String providerId) {
        if (!StringUtils.hasText(providerId)) {
            throw new IllegalArgumentException("카카오 social Id가 없습니다.");
        }

        try {
            webClient.post()
                    .uri("https://kapi.kakao.com/v1/user/unlink")
                    .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + adminKey)
                    .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                    // target_id_type을 user_id로 지정하고, 회원가입 시 저장해둔 고유 ID를 넘깁니다.
                    .bodyValue("target_id_type=user_id&target_id=" + providerId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            log.info("카카오 연결 끊기 성공 - providerId: {}", providerId);
        } catch (Exception e) {
            log.error("카카오 연결 끊기 실패 - providerId: {}, error: {}", providerId, e.getMessage());
            throw new IllegalStateException("카카오 탈퇴 처리 중 오류가 발생했습니다.", e);
        }
    }
}
