package com.todaktodot.TDTD.global.alert;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class DiscordNotificationService {

    @Value("${discord.webhook.enabled:false}")
    private boolean enabled;

    @Value("${discord.webhook.url:}")
    private String discordWebhookUrl;

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

        sendNotification(message);
    }

    public void sendErrorNotificationForBatch(String message) {
        String notiMessage = String.format("🚨 **[ %s 서버 배치 에러 발생]**\n\n- **Error Message**: %s",
                discordWebhookProfile, message);

        sendNotification(notiMessage);
    }

    private void sendNotification(String message) {
        if (!enabled) {
            return;
        }

        webClient.post()
                .uri(discordWebhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new DiscordMessage(message))
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe(
                        success -> {}, // 성공 시 처리할 로직 (필요시 작성)
                        error -> log.error("디스코드 웹훅 알림 전송 실패", error) // 에러 콜백
                );
    }
}
