package com.todaktodot.TDTD.domain.login.service.client;

import com.todaktodot.TDTD.domain.login.dto.response.SocialUserResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.Reader;
import java.io.StringReader;
import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class AppleClient {
    private final WebClient webClient = WebClient.create();

    @Value("${oauth.apple.url.auth}")
    private String authUrl;
    @Value("${oauth.apple.client-id}")
    private String clientId;
    @Value("${oauth.apple.login-key}")
    private String keyId;
    @Value("${oauth.apple.team-id}")
    private String teamId;
    @Value("${oauth.apple.key-path}")
    private String keyPath;

    public SocialUserResponse getUserInfo(String idToken) {

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", idToken);
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("client_secret", generateClientSecret());

        Map response;
        try {
            response = webClient.post()
                    .uri(authUrl)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body(BodyInserters.fromFormData(body))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(); // 동기 처리를 위해 block 사용 (Reactive 환경이면 Mono 반환 권장)
        } catch (Exception e) {
            throw new RuntimeException("애플 토큰 요청 실패: " + e.getMessage());
        }

        if (response == null || !response.containsKey("id_token")) {
            throw new RuntimeException("애플 응답에 id_token이 없습니다.");
        }

        String[] splitToken = ((String) response.get("id_token")).split("\\.");
        String unsignedToken = splitToken[0] + "." + splitToken[1] + ".";

        Claims claims = Jwts.parser()
                .build()
                .parseClaimsJwt(unsignedToken) // 서명이 없는 상태로 파싱
                .getBody();

        String sub = claims.getSubject();
        String email = claims.get("email", String.class);

        return new SocialUserResponse(
                (String) response.get(sub),
                (String) response.get(email),
                "AppleUser",
                "APPLE"
        );
    }

    private String generateClientSecret() {
        Map<String, Object> jwtHeader = new HashMap<>();
        jwtHeader.put("kid", keyId);
        jwtHeader.put("alg", "ES256");

        return Jwts.builder()
                .setHeaderParams(jwtHeader)
                .issuer(teamId)
                .audience().add("https://appleid.apple.com").and()
                .subject(clientId)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(Date.from(LocalDateTime.now().plusDays(30).atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(getPrivateKey(), SignatureAlgorithm.ES256)
                .compact();
    }

    private PrivateKey getPrivateKey() {
        ClassPathResource resource = new ClassPathResource(keyPath);
        try {
            String privateKey = new String(resource.getInputStream().readAllBytes());
            Reader pemReader = new StringReader(privateKey);
            PEMParser pemParser = new PEMParser(pemReader);
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            PrivateKeyInfo privateKeyInfo = (PrivateKeyInfo) pemParser.readObject();
            return converter.getPrivateKey(privateKeyInfo);

        } catch (Exception e) {
            String message = "Error converting private key from String";
            log.error(message);
            throw new RuntimeException(message, e);
        }
    }

}
