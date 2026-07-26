package com.todaktodot.TDTD.domain.login.service.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaktodot.TDTD.domain.login.dto.response.SocialUserResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.Reader;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class AppleClient {
    private final WebClient webClient = WebClient.create();
    private final ObjectMapper objectMapper;

    @Value("${oauth.apple.url.auth}")
    private String authUrl;
    @Value("${oauth.apple.client-id}")
    private String clientId;
    @Value("${oauth.apple.key-id}")
    private String keyId;
    @Value("${oauth.apple.team-id}")
    private String teamId;
    @Value("${oauth.apple.key-path}")
    private String keyPath;

    public SocialUserResponse getUserInfo(String authorizationCode, String name) {

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", authorizationCode);
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("client_secret", generateClientSecret());

        Map response;
        try {
            //authorization_code 로 요청
            response = webClient.post()
                    .uri(authUrl)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body(BodyInserters.fromFormData(body))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> Mono.error(new RuntimeException(errorBody)))
                    )
                    .bodyToMono(Map.class)
                    .block();

        } catch (Exception e) {
            throw new RuntimeException("애플 토큰 요청 실패: " + e.getMessage());
        }

        if (response == null || !response.containsKey("id_token")) {
            throw new RuntimeException("애플 응답에 id_token이 없습니다.");
        }

        String refreshToken = response.containsKey("refresh_token") ? response.get("refresh_token").toString() : null;
        Claims claims = verifyIdToken(response.get("id_token").toString());

        String sub = claims.getSubject();
        String email = claims.get("email", String.class);

        return SocialUserResponse.builder()
                .id(sub)
                .email(email)
                .name(name)
                .appleRefreshToken(refreshToken)
                .provider("APPLE")
                .build();
    }

    /**
     * IdToken 검증
     */
    public Claims verifyIdToken(String idToken) {

        Map<String, Object> header = parseHeader(idToken);
        String kid = (String) header.get("kid");

        Map<String, Object> keyResponse = getApplePublicKeys();
        List<Map<String, String>> keys =
                (List<Map<String, String>>) keyResponse.get("keys");

        Map<String, String> matchedKey = keys.stream()
                .filter(k -> kid.equals(k.get("kid")))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("일치하는 Apple 공개키가 없습니다."));

        try {
            PublicKey publicKey = generatePublicKey(matchedKey);

            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(idToken);

            Claims claims = jws.getPayload();

            validateClaims(claims);

            return claims;

        } catch (Exception e) {
            throw new RuntimeException("Apple id_token 검증 실패", e);
        }
    }

    /**
     * 애플 연결 해제
     */
    public void revokeToken(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new IllegalArgumentException("Refresh Token이 없습니다.");
        }

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", generateClientSecret()); // 기존에 만드신 메서드 재사용
        body.add("token", refreshToken);
        body.add("token_type_hint", "refresh_token");

        try {
            webClient.post()
                    .uri("https://appleid.apple.com/auth/revoke")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body(BodyInserters.fromFormData(body))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        log.error("애플 Revoke API 에러 응답: {}", errorBody);
                                        return Mono.error(new RuntimeException("애플 연동 해제 실패: " + errorBody));
                                    })
                    )
                    .bodyToMono(Void.class)
                    .block();

            log.info("Apple 연동 해제 성공");
        } catch (Exception e) {
            log.error("Apple 연동 해제 중 오류 발생: {}", e.getMessage());
            throw new IllegalStateException("Apple 탈퇴 처리 실패", e);
        }
    }

    /**
     * 토큰 헤더 파싱
     */
    private Map<String, Object> parseHeader(String token) {
        try {
            String header = token.split("\\.")[0];
            String decoded = new String(Base64.getUrlDecoder().decode(header));
            return objectMapper.readValue(decoded, Map.class);
        }
        catch (JsonProcessingException e) {
            throw new IllegalStateException("Json Parsing Error");
        }
    }

    /**
     * Public Key 생성
     */
    private PublicKey generatePublicKey(Map<String, String> jwk) throws Exception {

        byte[] nBytes = Base64.getUrlDecoder().decode(jwk.get("n"));
        byte[] eBytes = Base64.getUrlDecoder().decode(jwk.get("e"));

        BigInteger n = new BigInteger(1, nBytes);
        BigInteger e = new BigInteger(1, eBytes);

        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(n, e);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return keyFactory.generatePublic(keySpec);
    }

    /**
     * Claim 검증
     */
    private void validateClaims(Claims claims) {
        // 1. 발급자 검증
        if (!"https://appleid.apple.com".equals(claims.getIssuer())) {
            throw new IllegalStateException("유효하지 않은 issuer 입니다.");
        }

        // 2. audience 검증
        if (!claims.getAudience().contains(clientId)) {
            throw new IllegalStateException("유효하지 않은 audience 입니다.");
        }

        // 3. 만료시간 검증
        if (claims.getExpiration().before(new Date())) {
            throw new IllegalStateException("IdToken의 기한이 만료되었습니다.");
        }
    }

    /**
     * Apple 공개키 조회
     */
    private Map<String, Object> getApplePublicKeys() {
        return webClient.get()
                .uri("https://appleid.apple.com/auth/keys")
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    /**
     * client_secret 생성
     */
    private String generateClientSecret() {
        Map<String, Object> jwtHeader = new HashMap<>();
        jwtHeader.put("kid", keyId);
        jwtHeader.put("alg", "ES256");

        return Jwts.builder()
                .setHeaderParams(jwtHeader)
                .issuer(teamId)
                .claim("aud", "https://appleid.apple.com")
                .subject(clientId)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(Date.from(LocalDateTime.now().plusDays(60).atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(getPrivateKey(), SignatureAlgorithm.ES256)
                .compact();
    }

    /**
     * private 생성
     */
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
