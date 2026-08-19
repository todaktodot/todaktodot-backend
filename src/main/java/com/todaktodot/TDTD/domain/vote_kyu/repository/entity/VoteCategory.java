package com.todaktodot.TDTD.domain.vote_kyu.repository.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VoteCategory {
    LOVE("연애관", "감정 표현, 소통 방식, 관계 기준, 갈등 해결 스타일"),
    ECONOMY("경제관", "소비, 저축, 투자 습관 등 금전 관련"),
    LIFESTYLE("생활관", "일상 루틴, 생활습관, 가사 분담, 결혼/자녀 등");

    private final String displayName;
    private final String description;
}
