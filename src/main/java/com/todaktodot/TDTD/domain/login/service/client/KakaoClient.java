package com.todaktodot.TDTD.domain.login.service.client;

import com.todaktodot.TDTD.domain.login.dto.response.SocialUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
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
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        //현재는 필요없어서 사용하지 않음
//        Map<String, Object> kakaoAccount = (Map<String, Object>) response.get("kakao_account");
//        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        //TODO : 예외처리
        if (response == null || response.get("id") == null) {
            throw new RuntimeException();
        }

        return SocialUserResponse.builder()
                .id(response.get("id").toString())
                .provider("KAKAO")
                        .build();
    }
}
