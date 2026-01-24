package com.todaktodot.TDTD.global.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 애플리케이션 시작 완료 시 n8n webhook을 통해 Discord로 알림을 보내는 리스너
 */
@Slf4j
@Component
public class DeployNotificationListener {

    @Value("${deploy.notification.enabled:false}")
    private boolean notificationEnabled;

    @Value("${deploy.notification.n8n-webhook-url:}")
    private String n8nWebhookUrl;

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    @Value("${server.port:8080}")
    private int serverPort;

    private final WebClient webClient;

    public DeployNotificationListener(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("========== 배포 알림 리스너 시작 ==========");
        log.info("알림 활성화 여부: {}", notificationEnabled);
        log.info("Webhook URL: {}", n8nWebhookUrl);

        if (!notificationEnabled) {
            log.info("배포 알림이 비활성화되어 있습니다.");
            return;
        }

        if (n8nWebhookUrl == null || n8nWebhookUrl.isBlank()) {
            log.warn("n8n webhook URL이 설정되지 않았습니다.");
            return;
        }

        Map<String, Object> payload = Map.of(
            "event", "APPLICATION_READY",
            "profile", activeProfile,
            "port", serverPort,
            "timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "hostname", getHostname()
        );

        sendNotification(payload);
    }

    private void sendNotification(Map<String, Object> payload) {
        webClient.post()
            .uri(n8nWebhookUrl)
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(String.class)
            .doOnSuccess(response -> log.info("배포 알림 전송 성공: {}", response))
            .doOnError(error -> log.error("배포 알림 전송 실패: {}", error.getMessage()))
            .subscribe();
    }

    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
