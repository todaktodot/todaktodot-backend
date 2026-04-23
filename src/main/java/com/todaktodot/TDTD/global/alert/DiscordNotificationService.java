package com.todaktodot.TDTD.global.alert;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class DiscordNotificationService {

    @Value("${discord.webhook.enabled:false}")
    private boolean enabled;

    @Value("${discord.webhook.api-url:}")
    private String discordApiWebhookUrl;

    @Value("${discord.webhook.batch-url:}")
    private String discordBatchWebhookUrl;

    @Value("${discord.webhook.profile:local}")
    private String discordWebhookProfile;

    private final WebClient webClient;

    public DiscordNotificationService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    // 디스코드 메시지 페이로드를 위한 record
    public record DiscordMessage(String content) {}

    // 기존 웹 요청용 메서드
    public void sendErrorNotificationForAPI(Exception e, HttpServletRequest request) {
        String message = String.format("🚨 **[ %s 서버 API 에러 발생]**\n- **에러 위치**: `%s`\n- **Error**: %s",
                discordWebhookProfile, request.getRequestURI(), e.getMessage());

        sendNotification(discordApiWebhookUrl, message, "API");
    }

    public void sendErrorNotificationForBatch(String message) {
        String notiMessage = String.format("🚨 **[ %s 서버 배치 에러 발생]**\n\n- **Error Message**: %s",
                discordWebhookProfile, message);

        sendNotification(discordBatchWebhookUrl, notiMessage, "BATCH_ERROR");
    }

    public void sendSuccessNotificationForBatch(String message) {
        String notiMessage = String.format("✅ **[ %s 서버 배치 성공]**\n\n%s",
                discordWebhookProfile, message);

        sendNotification(discordBatchWebhookUrl, notiMessage, "BATCH_SUCCESS");
    }

    private void sendNotification(String webhookUrl, String message, String notificationType) {
        if (!enabled) {
            return;
        }

        if (!StringUtils.hasText(webhookUrl)) {
            log.warn("디스코드 웹훅 URL이 비어 있어 {} 알림을 전송하지 않음", notificationType);
            return;
        }

        webClient.post()
                .uri(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new DiscordMessage(message))
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe(
                        success -> {},
                        error -> log.error("디스코드 웹훅 {} 알림 전송 실패", notificationType, error)
                );
    }
}
