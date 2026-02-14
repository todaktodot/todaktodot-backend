package com.todaktodot.TDTD.domain.login.service.client;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.todaktodot.TDTD.domain.login.dto.response.SocialUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GoogleClient {

    @Value("${oauth.google.ios-client-id-dev}")
    private String iosClientIdDev;

    @Value("${oauth.google.ios-client-id-prod}")
    private String iosClientIdProd;

    @Value("${oauth.google.aos-client-id-dev}")
    private String aosClientIdDev;

    @Value("${oauth.google.aos-client-id-prod}")
    private String aosClientIdProd;

    @Value("${oauth.google.web-client-id}")
    private String webClientId;

    public SocialUserResponse getUserInfo(String idTokenStr) {
        List<String> clientIds = new ArrayList<>();
        clientIds.add(iosClientIdDev);
        clientIds.add(iosClientIdProd);
        clientIds.add(aosClientIdDev);
        clientIds.add(aosClientIdProd);
        clientIds.add(webClientId);

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    new GsonFactory()
            )
                    .setAudience(clientIds)
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenStr);

            if (idToken == null) {
                throw new IllegalStateException("유효하지 않은 IdToken 입니다.");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            return SocialUserResponse.builder()
                .id( payload.getSubject())
                .email(payload.getEmail())
                .name((String) payload.get("name"))
                .provider("GOOGLE")
                .build();

        } catch (Exception e) {
            throw new IllegalStateException("IdToken 검증에 실패했습니다. : " + idTokenStr);
        }
    }
}
