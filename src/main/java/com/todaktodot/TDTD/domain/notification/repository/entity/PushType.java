package com.todaktodot.TDTD.domain.notification.repository.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.todaktodot.TDTD.domain.notification.repository.entity.PushCategory.*;

@Getter
@RequiredArgsConstructor
public enum PushType {

    // 정보성 알림 (마케팅 동의 불필요)
    DAILY_CARD("DAILY_CARD", "데일리카드 도착", INFORMATIONAL),
    POKE("POKE", "콕 찌르기", INFORMATIONAL),
    PARTNER_ANSWER("PARTNER_ANSWER", "파트너 답변 완료", INFORMATIONAL),
    BOTH_ANSWER("BOTH_ANSWER", "모두 답변 완료", INFORMATIONAL),
    CONNECT_COUPLE("CONNECT_COUPLE", "커플 연결 완료", INFORMATIONAL),
    EMOJI_REACTION("EMOJI_REACTION", "이모지 반응 완료", INFORMATIONAL),
    AI_FEEDBACK("AI_FEEDBACK", "AI 피드백 도착", INFORMATIONAL),
    AI_REPORT("AI_REPORT", "AI 리포트 도착", INFORMATIONAL),

    // 광고성 알림 (마케팅 동의 필요)
    EVENT("EVENT", "이벤트 알림", ADVERTISING),
    PROMOTION("PROMOTION", "프로모션 알림", ADVERTISING),
    RE_ENGAGEMENT("RE_ENGAGEMENT", "재방문 유도", ADVERTISING);

    private final String code;
    private final String description;
    private final PushCategory category;

    /**
     * 마케팅 동의가 필요한 푸시인지 확인
     */
    public boolean isMarketingConsentRequired() {
        return category.isMarketingConsentRequired();
    }

    /**
     * 광고성 푸시인지 확인
     */
    public boolean isAdvertising() {
        return category == ADVERTISING;
    }
}
