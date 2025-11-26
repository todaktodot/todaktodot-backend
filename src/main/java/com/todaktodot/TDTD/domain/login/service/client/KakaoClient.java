package com.todaktodot.TDTD.domain.login.service.client;

import com.todaktodot.TDTD.domain.login.dto.response.SocialUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class KakaoClient {
    private final WebClient webClient = WebClient.create();

    public SocialUserResponse getUserInfo(String accessToken) {
        Map<String, Object> response = webClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        Map<String, Object> kakaoAccount = (Map<String, Object>) response.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return new SocialUserResponse(
                response.get("id").toString(),
                (String) kakaoAccount.get("email"),
                (String) profile.get("nickname"),
                "KAKAO"
        );
    }
}
