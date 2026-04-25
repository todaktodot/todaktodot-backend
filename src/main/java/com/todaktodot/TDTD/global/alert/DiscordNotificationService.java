package com.todaktodot.TDTD.global.alert;

import com.todaktodot.TDTD.domain.login.respository.entity.UserPrincipal;
import com.todaktodot.TDTD.global.config.RequestLoggingFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.WebUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class DiscordNotificationService {

    @Value("${discord.webhook.enabled:false}")
    private boolean enabled;

    @Value("${discord.webhook.api-url:}")
    private String discordApiWebhookUrl;

    @Value("${discord.webhook.batch-url:}")
    private String discordBatchWebhookUrl;

    @Value("${discord.webhook.signup-url:}")
    private String discordSignUpWebhookUrl;

    @Value("${discord.webhook.extra-api-urls:}")
    private String extraApiWebhookUrls;

    @Value("${discord.webhook.extra-batch-urls:}")
    private String extraBatchWebhookUrls;


    @Value("${discord.webhook.profile:local}")
    private String discordWebhookProfile;

    private final WebClient webClient;

    public DiscordNotificationService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public record DiscordWebhookPayload(String username, List<DiscordEmbed> embeds) {}

    public record DiscordEmbed(
            String title,
            String description,
            Integer color,
            List<DiscordEmbedField> fields,
            DiscordEmbedFooter footer,
            String timestamp
    ) {}

    public record DiscordEmbedField(String name, String value, Boolean inline) {}

    public record DiscordEmbedFooter(String text) {}

    public void sendErrorNotificationForAPI(Exception e, HttpServletRequest request) {
        String traceId = MDC.get(RequestLoggingFilter.TRACE_ID_KEY);
        String requestBody = extractRequestBody(request);
        String queryString = request.getQueryString();

        List<DiscordEmbedField> fields = new ArrayList<>();
        fields.add(new DiscordEmbedField("요청 API", formatCodeBlock(request.getRequestURI()), false));
        fields.add(new DiscordEmbedField("Method", formatCodeBlock(request.getMethod()), true));

        Long userId = extractUserId();
        if (userId != null) {
            fields.add(new DiscordEmbedField("UserId", formatCodeBlock(String.valueOf(userId)), true));
        }

        if (StringUtils.hasText(traceId)) {
            fields.add(new DiscordEmbedField("TraceId", formatCodeBlock(traceId), true));
        }
        if (StringUtils.hasText(queryString)) {
            fields.add(new DiscordEmbedField("Query", formatCodeBlock(truncate(queryString, 900)), false));
        }
        if (StringUtils.hasText(requestBody)) {
            fields.add(new DiscordEmbedField("Request Body", formatJsonBlock(truncate(requestBody, 900)), false));
        }
        fields.add(new DiscordEmbedField("원인", formatCodeBlock(truncate(defaultString(e.getMessage(), e.getClass().getSimpleName()), 900)), false));

        DiscordWebhookPayload payload = new DiscordWebhookPayload(
                buildUsername(),
                List.of(new DiscordEmbed(
                        "🚨 API 에러 발생",
                        String.format("**%s** 서버에서 API 호출 중 예외 발생.", discordWebhookProfile),
                        0xED4245,
                        fields,
                        new DiscordEmbedFooter(discordWebhookProfile + " • API ERROR"),
                        OffsetDateTime.now().toString()
                ))
        );

        sendNotification(resolveWebhookUrls(discordApiWebhookUrl, extraApiWebhookUrls), payload, "API");
    }

    public void sendErrorNotificationForBatch(String message) {
        DiscordWebhookPayload payload = new DiscordWebhookPayload(
                buildUsername(),
                List.of(new DiscordEmbed(
                        "🚨 배치 에러 발생",
                        String.format("**%s** 서버 배치 실행 중 오류 발생.", discordWebhookProfile),
                        0xED4245,
                        List.of(new DiscordEmbedField("Error Message", truncate(defaultString(message, "알 수 없는 오류"), 900), false)),
                        new DiscordEmbedFooter(discordWebhookProfile + " • BATCH ERROR"),
                        OffsetDateTime.now().toString()
                ))
        );

        sendNotification(resolveWebhookUrls(discordBatchWebhookUrl, extraBatchWebhookUrls), payload, "BATCH_ERROR");
    }

    public void sendSuccessNotificationForBatch(String message) {
        DiscordWebhookPayload payload = new DiscordWebhookPayload(
                buildUsername(),
                List.of(new DiscordEmbed(
                        "✅ 배치 성공",
                        String.format("**%s** 서버 배치 정상 완료.", discordWebhookProfile),
                        0x57F287,
                        List.of(new DiscordEmbedField("Result", truncate(defaultString(message, "완료"), 900), false)),
                        new DiscordEmbedFooter(discordWebhookProfile + " • BATCH SUCCESS"),
                        OffsetDateTime.now().toString()
                ))
        );

        sendNotification(resolveWebhookUrls(discordBatchWebhookUrl, extraBatchWebhookUrls), payload, "BATCH_SUCCESS");
    }

    public void sendSuccessNotificationForNewUser(String message) {
        DiscordWebhookPayload payload = new DiscordWebhookPayload(
                buildUsername(),
                List.of(new DiscordEmbed(
                        "👦🏻 신규 가입",
                        String.format("**%s** 서버 신규 가입 발생.", discordWebhookProfile),
                        0x57F287,
                        List.of(new DiscordEmbedField("Result", truncate(defaultString(message, "신규 가입"), 900), false)),
                        new DiscordEmbedFooter(discordWebhookProfile + " • NEW USER"),
                        OffsetDateTime.now().toString()
                ))
        );

        sendNotification(resolveWebhookUrls(discordSignUpWebhookUrl, ""), payload, "NEW_USER");
    }

    private void sendNotification(List<String> webhookUrls, DiscordWebhookPayload payload, String notificationType) {
        if (!enabled) {
            return;
        }

        if (webhookUrls == null || webhookUrls.isEmpty()) {
            log.warn("디스코드 웹훅 URL이 비어 있어 {} 알림을 전송하지 않음", notificationType);
            return;
        }

        webhookUrls.forEach(webhookUrl -> webClient.post()
                .uri(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe(
                        success -> {},
                        error -> log.error("디스코드 웹훅 {} 알림 전송 실패", notificationType, error)
                ));
    }

    private List<String> resolveWebhookUrls(String primaryWebhookUrl, String extraWebhookUrls) {
        String combined = (StringUtils.hasText(primaryWebhookUrl) ? primaryWebhookUrl + "," : "") +
                (StringUtils.hasText(extraWebhookUrls) ? extraWebhookUrls : "");

        if (!StringUtils.hasText(combined)) {
            return List.of();
        }

        return Arrays.stream(combined.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    private String extractRequestBody(HttpServletRequest request) {
        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper == null) {
            return null;
        }

        byte[] buf = wrapper.getContentAsByteArray();
        if (buf == null || buf.length == 0) {
            return null;
        }

        Charset charset = StandardCharsets.UTF_8;
        String encoding = wrapper.getCharacterEncoding();
        if (StringUtils.hasText(encoding)) {
            try {
                charset = Charset.forName(encoding);
            } catch (Exception ignored) {
                charset = StandardCharsets.UTF_8;
            }
        }

        return new String(buf, charset).trim();
    }

    private Long extractUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }

        return null;
    }

    private String buildUsername() {
        return StringUtils.hasText(discordWebhookProfile)
                ? discordWebhookProfile + " 오류 전달"
                : "오류전달";
    }

    private String formatCodeBlock(String value) {
        return "```\n" + value + "\n```";
    }

    private String formatJsonBlock(String value) {
        return "```json\n" + value + "\n```";
    }

    private String defaultString(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private String truncate(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + " ...[truncated]";
    }
}
