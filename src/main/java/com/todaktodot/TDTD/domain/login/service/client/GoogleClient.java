package com.todaktodot.TDTD.domain.login.service.client;

import com.todaktodot.TDTD.domain.login.dto.response.SocialUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GoogleClient {
    private final WebClient webClient = WebClient.create();

    public SocialUserResponse getUserInfo(String idToken) {
        // 구글의 tokeninfo 엔드포인트 사용
        Map<String, Object> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("oauth2.googleapis.com")
                        .path("/tokeninfo")
                        .queryParam("id_token", idToken)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return new SocialUserResponse(
                (String) response.get("sub"),
                (String) response.get("email"),
                (String) response.get("name"),
                "GOOGLE"
        );
    }
}
