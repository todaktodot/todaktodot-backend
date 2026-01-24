package com.todaktodot.TDTD.domain.login.service.client;

import com.todaktodot.TDTD.domain.login.dto.response.SocialUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GoogleClient {
    private final WebClient webClient = WebClient.create();

    public SocialUserResponse getUserInfo(String accessToken) {
        // 구글의 tokeninfo 엔드포인트 사용
        Map<String, Object> response = webClient.get()
                .uri("https://openidconnect.googleapis.com/v1/userinfo")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || response.get("sub") == null) {
            throw new RuntimeException();
        }

        return SocialUserResponse.builder()
                .id( (String) response.get("sub"))
                .email((String) response.get("email"))
                .name((String) response.get("name"))
                .provider("GOOGLE")
                .build();
    }
}
