package com.todaktodot.TDTD.domain.vote.repository.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 투표의 진행 상태. 저장하지 않고 CLOSED_AT 과 현재 시각을 비교해 계산한다.
@Getter
@RequiredArgsConstructor
@Schema(description = "투표 진행 상태 - ACTIVE: 진행중, CLOSED: 마감")
public enum VoteStatus {
    ACTIVE("진행중"),
    CLOSED("마감");

    private final String description;
}
