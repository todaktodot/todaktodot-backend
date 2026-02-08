package com.todaktodot.TDTD.admin.prompt.repository.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PromptType {

    CARD_GENERATION("데일리카드 질문 생성"),
    CARD_FEEDBACK("데일리카드 답변 피드백"),
    REPORT_INSIGHT("AI리포트 인사이트");

    private final String displayName;
}
