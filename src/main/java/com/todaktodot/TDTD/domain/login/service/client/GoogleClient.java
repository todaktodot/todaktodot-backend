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
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GoogleClient {
    //임시
    private static final String IOS_GOOGLE_CLIENT_ID = "1034525705170-kmpucuffctgnj0rmre9kflolqj8b00fa.apps.googleusercontent.com";
    private static final String AOS_GOOGLE_CLIENT_ID = "1034525705170-glmg4nqee474ru5br4iam1qac8i26r4i.apps.googleusercontent.com";
    private static final String WEB_GOOGLE_CLIENT_ID = "1034525705170-v3nmvs14sgm5pg106meajjken9ah5vvn.apps.googleusercontent.com";

//    @Value("${google.client-id}")
//    private String googleClientId;

    public SocialUserResponse getUserInfo(String idTokenStr) {
        List<String> clientIds = new ArrayList<>();
        clientIds.add(IOS_GOOGLE_CLIENT_ID);
        clientIds.add(AOS_GOOGLE_CLIENT_ID);
        clientIds.add(WEB_GOOGLE_CLIENT_ID);

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
