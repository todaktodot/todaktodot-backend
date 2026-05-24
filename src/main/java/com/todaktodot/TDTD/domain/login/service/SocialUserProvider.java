package com.todaktodot.TDTD.domain.login.service;

import com.todaktodot.TDTD.domain.login.dto.response.SocialUserResponse;
import com.todaktodot.TDTD.domain.login.service.client.AppleClient;
import com.todaktodot.TDTD.domain.login.service.client.GoogleClient;
import com.todaktodot.TDTD.domain.login.service.client.KakaoClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocialUserProvider {

    private final KakaoClient kakaoClient;
    private final GoogleClient googleClient;
    private final AppleClient appleClient;

    public SocialUserResponse getLoginedSocialUser(String provider, String token, String appleName) {
        return switch (provider.toUpperCase()) {
            case "KAKAO" -> kakaoClient.getUserInfo(token);
            case "GOOGLE" -> googleClient.getUserInfo(token);
            case "APPLE" -> appleClient.getUserInfo(token, appleName);
            default -> throw new IllegalArgumentException("Unknown provider: " + provider);
        };
    }

    public void revokeSocialUser(String provider, String providerId, String refreshToken) {
        switch (provider.toUpperCase()) {
            case "KAKAO" -> kakaoClient.revokeUser(providerId);
            case "GOOGLE" -> {}
            case "APPLE" -> appleClient.revokeToken(refreshToken);
            default -> throw new IllegalArgumentException("Unknown provider: " + provider);
        };
    }
}
