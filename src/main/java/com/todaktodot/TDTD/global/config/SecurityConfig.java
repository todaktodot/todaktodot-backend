package com.todaktodot.TDTD.global.config;

import com.todaktodot.TDTD.global.jwt.JwtTokenProvider;
import com.todaktodot.TDTD.global.security.JwtAuthenticationEntryPoint;
import com.todaktodot.TDTD.global.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                // 1. CSRF 해제
                .csrf(AbstractHttpConfigurer::disable)
                // 2. HTTP Basic & Form Login 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                // 3. 세션 설정 (Stateless)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 4. 예외 처리
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                // 5. 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/login/test").permitAll()
                        .anyRequest().authenticated()
                )
                // 6. JWT 필터 등록
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                );
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web -> web.ignoring().requestMatchers("/ws/**", "/v3/api-docs/**", "/swagger-resources/**", "/swagger-ui/**", "/favicon.ico"));
    }
}
