package com.todaktodot.TDTD.domain.vote.repository.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 투표의 노출 상태. 신고 누적이나 어드민 조작으로만 바뀌며 VOTE.STATUS 컬럼에 저장한다.
// DRAFT 는 게시 전 초안으로 피드·마이페이지·상세에 노출하지 않는다.
@Getter
@RequiredArgsConstructor
@Schema(description = "투표 노출 상태 - DRAFT: 초안, POSTED: 게시, HIDDEN: 숨김")
public enum VoteDisplayStatus {
    DRAFT("초안"),
    POSTED("게시"),
    HIDDEN("숨김");

    private final String description;
}
