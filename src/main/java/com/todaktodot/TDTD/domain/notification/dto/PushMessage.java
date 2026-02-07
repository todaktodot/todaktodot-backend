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
                .title("오늘의 질문이 도착했어요 💌")
                .body("파트너와 함께 답변해보세요!")
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
    public static PushMessage poke(String senderNickname) {
        return PushMessage.builder()
                .title("콕! 👆")
                .body(senderNickname + "님이 찔렀어요!")
                .pushType(PushType.POKE)
                .data(Map.of(
                        "type", PushType.POKE.getCode()
                ))
                .build();
    }

    /**
     * 파트너 답변 완료 알림 생성
     */
    public static PushMessage partnerAnswer(Long coupleId, Long coupleDailyCardId) {
        return PushMessage.builder()
                .title("파트너가 답변했어요! ✨")
                .body("어떤 답변을 했는지 확인해보세요")
                .pushType(PushType.PARTNER_ANSWER)
                .data(Map.of(
                        "type", PushType.PARTNER_ANSWER.getCode(),
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
}
