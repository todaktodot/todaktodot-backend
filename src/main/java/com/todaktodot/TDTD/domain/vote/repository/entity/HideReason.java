package com.todaktodot.TDTD.domain.vote.repository.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "투표 숨김 사유 - AUTO: 신고 누적 자동 숨김, ADMIN: 어드민 숨김")
public enum HideReason {
    AUTO("신고 누적 자동 숨김"),
    ADMIN("어드민 숨김");

    private final String description;
}
