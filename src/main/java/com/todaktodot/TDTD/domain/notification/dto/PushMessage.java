package com.todaktodot.TDTD.domain.notification.dto;

import com.todaktodot.TDTD.domain.notification.repository.entity.PushType;
import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
public class PushMessage {

    private final String title;
    private final String body;
    private final PushType pushType;

    @Builder.Default
    private final Map<String, String> data = new HashMap<>();

    /**
     * 데일리카드 도착 알림 생성
     */
    public static PushMessage dailyCard(Long coupleId, Long coupleDailyCardId) {
        return PushMessage.builder()
                .title("✉️오늘의 질문이 도착했어요")
                .body("연인보다 빨리 답변 남기러 가볼까요?")
                .pushType(PushType.DAILY_CARD)
                .data(Map.of(
                        "type", PushType.DAILY_CARD.getCode(),
                        "coupleId", String.valueOf(coupleId),
                        "coupleDailyCardId", String.valueOf(coupleDailyCardId)
                ))
                .build();
    }

    /**
     * 콕 찌르기 알림 생성
     */
    public static PushMessage poke(String senderNickname, Long coupleDailyCardId) {
        return PushMessage.builder()
                .title("👉" + senderNickname + "님이 콕 찔렀어요!")
                .body("오늘의 질문에 답변해주세요.")
                .pushType(PushType.POKE)
                .data(Map.of(
                        "type", PushType.POKE.getCode(),
                        "coupleDailyCardId", String.valueOf(coupleDailyCardId)
                ))
                .build();
    }

    /**
     * 파트너 답변 완료 알림 생성
     */
    public static PushMessage partnerAnswer(Long coupleId, Long coupleDailyCardId) {
        return PushMessage.builder()
                .title("📄연인이 방금 답변을 남겼어요!")
                .body("내가 답해야 서로 확인할 수 있어요.")
                .pushType(PushType.PARTNER_ANSWER)
                .data(Map.of(
                        "type", PushType.PARTNER_ANSWER.getCode(),
                        "coupleId", String.valueOf(coupleId),
                        "coupleDailyCardId", String.valueOf(coupleDailyCardId)
                ))
                .build();
    }

    /**
     * 모두 답변 완료 알림 생성
     */
    public static PushMessage bothAnswer(Long coupleId, Long coupleDailyCardId) {
        return PushMessage.builder()
                .title("💜두 사람 모두 답변을 완료했어요!")
                .body("서로의 마음을 확인해보세요.")
                .pushType(PushType.BOTH_ANSWER)
                .data(Map.of(
                        "type", PushType.BOTH_ANSWER.getCode(),
                        "coupleId", String.valueOf(coupleId),
                        "coupleDailyCardId", String.valueOf(coupleDailyCardId)
                ))
                .build();
    }

    /**
     * AI 피드백 도착 알림 생성
     */
    public static PushMessage aiFeedback(Long coupleId, Long feedbackId) {
        return PushMessage.builder()
                .title("AI 피드백이 도착했어요! 🤖")
                .body("오늘의 대화를 분석한 결과를 확인해보세요")
                .pushType(PushType.AI_FEEDBACK)
                .data(Map.of(
                        "type", PushType.AI_FEEDBACK.getCode(),
                        "coupleId", String.valueOf(coupleId),
                        "feedbackId", String.valueOf(feedbackId)
                ))
                .build();
    }

    /**
     * AI 리포트 도착 알림 생성
     */
    public static PushMessage aiReport(Long coupleId, Long reportId) {
        return PushMessage.builder()
                .title("AI 리포트가 발행되었어요! 🤖")
                .body("우리의 일주일을 지금 바로 확인해보세요.")
                .pushType(PushType.AI_REPORT)
                .data(Map.of(
                        "type", PushType.AI_REPORT.getCode(),
                        "coupleId", String.valueOf(coupleId),
                        "reportId", String.valueOf(reportId)
                ))
                .build();
    }

    /**
     * 모두 답변 완료 알림 생성
     */
    public static PushMessage connectCouple(Long coupleId) {
        return PushMessage.builder()
                .title("커플 연결 완료!")
                .body("이제 둘만의 대화를 시작할 수 있어요! 닉네임을 입력하러 가볼까요?")
                .pushType(PushType.CONNECT_COUPLE)
                .data(Map.of(
                        "type", PushType.CONNECT_COUPLE.getCode(),
                        "coupleId", String.valueOf(coupleId)
                ))
                .build();
    }
}
