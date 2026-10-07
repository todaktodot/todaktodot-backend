package com.todaktodot.TDTD.domain.vote.repository.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "투표 후보 처리 상태 - PENDING: 대기, APPROVED: 등록됨, REJECTED: 반려")
public enum VoteCandidateStatus {
    PENDING("대기"),
    APPROVED("등록됨"),
    REJECTED("반려");

    private final String description;
}
