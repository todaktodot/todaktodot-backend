package com.todaktodot.TDTD.domain.vote.repository.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "투표 카테고리 - LOVE: 연애관, ECONOMY: 경제관, LIFESTYLE: 생활관")
public enum VoteCategory {
    LOVE("연애관"),
    ECONOMY("경제관"),
    LIFESTYLE("생활관");

    private final String description;
}
