package com.todaktodot.TDTD.global.jwt;

import com.todaktodot.TDTD.global.security.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider implements InitializingBean {

    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 5;  // 5시간
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;  // 7일

    private Key key;

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Override
    public void afterPropertiesSet() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * AccessToken 생성
     */
    public String createAccessToken(Long userId, String email, Role role) {
        Date accessTokenExpiredAt = new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE_TIME);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .expiration(accessTokenExpiredAt)
                .claim("roles", role.getRole())
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * RefreshToken 생성
     */
    public String createRefreshToken(Long userId) {
        Date refreshTokenExpiredAt = new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE_TIME);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .expiration(refreshTokenExpiredAt)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 사용자 정보 추출
     */
    public String extractSubject(String accessToken) {
        Claims claims = parseClaims(accessToken);
        return claims.getSubject();
    }

    /**
     * Claim 추출
     */
    public Claims parseClaims(String accessToken) {
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(accessToken)
                .getBody();
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .setSigningKey(key).build()
                    .parseClaimsJws(token);

            return claims.getBody().getExpiration().after(new Date());
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("잘못된 JWT 서명입니다.");
            throw e;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰입니다.");
            throw e;
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰입니다.");
            throw e;
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 잘못되었습니다.");
            throw e;
        } catch (Exception e) {
            log.warn("예외가 발생했습니다.");
            throw e;
        }
    }
}
