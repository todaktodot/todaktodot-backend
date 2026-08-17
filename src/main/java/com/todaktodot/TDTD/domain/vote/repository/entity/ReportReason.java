package com.todaktodot.TDTD.domain.vote.repository.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "투표 신고 사유 - ABUSE: 욕설/비하 발언, OBSCENE: 음란물/선정적 내용, SPAM: 스팸/도배, "
        + "ADVERTISEMENT: 광고/홍보, PRIVACY: 개인정보 노출, ILLEGAL: 불법 정보, DISLIKE: 마음에 들지 않음")
public enum ReportReason {
    ABUSE("욕설/비하 발언"),
    OBSCENE("음란물/선정적 내용"),
    SPAM("스팸/도배"),
    ADVERTISEMENT("광고/홍보"),
    PRIVACY("개인정보 노출"),
    ILLEGAL("불법 정보"),
    DISLIKE("마음에 들지 않음");

    private final String description;
}
