package com.todaktodot.TDTD.domain.login.service.client;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.todaktodot.TDTD.domain.login.dto.response.SocialUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class GoogleClient {
    //임시
    private static final String GOOGLE_CLIENT_ID = "1034525705170-kmpucuffctgnj0rmre9kflolqj8b00fa.apps.googleusercontent.com";

//    @Value("${google.client-id}")
//    private String googleClientId;

    public SocialUserResponse getUserInfo(String idTokenStr) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    new GsonFactory()
            )
                    .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
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
