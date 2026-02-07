package com.todaktodot.TDTD.domain.notification.repository.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PushCategory {

    INFORMATIONAL("정보성", false),  // 서비스 이용에 필수적인 알림
    ADVERTISING("광고성", true);      // 마케팅/프로모션 알림

    private final String description;
    private final boolean marketingConsentRequired;  // 마케팅 동의 필요 여부
}
