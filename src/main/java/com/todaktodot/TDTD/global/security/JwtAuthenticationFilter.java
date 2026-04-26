package com.todaktodot.TDTD.global.security;

import com.todaktodot.TDTD.domain.login.respository.entity.UserPrincipal;
import com.todaktodot.TDTD.global.exception.CustomJwtException;
import com.todaktodot.TDTD.global.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        try {
            if (token != null && jwtTokenProvider.validateToken(token)) {
                Claims claims = jwtTokenProvider.parseClaims(token);

                // 토큰 정보로 UserPrincipal 생성
                Long userId = Long.parseLong(claims.getSubject());
                String role = claims.get("roles", String.class);

                UserPrincipal userPrincipal = new UserPrincipal(userId, Collections.singleton(new SimpleGrantedAuthority(role)));

                // SecurityContext에 등록
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }}
        catch (CustomJwtException e) {
            request.setAttribute("exception", e.getMessage());
            log.warn("JwtAuthFilter: Caught CustomJwtException {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
