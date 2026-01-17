package com.todaktodot.TDTD.global.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * API 요청에 대한 로깅 필터
 * - 정적 리소스 요청은 로깅에서 제외
 * - 각 요청마다 고유한 traceId를 생성하여 MDC에 저장
 */
@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_KEY = "traceId";

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 로깅에서 제외할 경로 패턴 목록
     */
    private static final List<String> EXCLUDED_PATHS = List.of(
            // 정적 리소스
            "/css/**",
            "/js/**",
            "/images/**",
            "/image/**",
            "/favicon.ico",
            // Swagger 관련
            "/swagger-ui/**",
            "/v3/api-docs/**",
            // Actuator
            "/actuator/**"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return EXCLUDED_PATHS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = generateTraceId();
        MDC.put(TRACE_ID_KEY, traceId);

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("HTTP {} {} -> {} ({}ms)",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duration);
            MDC.remove(TRACE_ID_KEY);
        }
    }

    /**
     * traceId 생성
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
