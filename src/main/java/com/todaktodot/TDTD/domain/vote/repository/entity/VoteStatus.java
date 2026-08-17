package com.todaktodot.TDTD.domain.vote.repository.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "투표 노출 상태 - POSTED: 게시, HIDDEN: 숨김")
public enum VoteStatus {
    POSTED("게시"),
    HIDDEN("숨김");

    private final String description;
}
