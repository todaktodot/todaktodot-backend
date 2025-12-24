package com.todaktodot.TDTD.domain.couple.repository.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description =
    """
    연인 관계 단계
    - DATING              : 연애중
    - LIVING_TOGETHER     : 동거중
    - PREPARING_MARRIAGE  : 결혼 준비중
    - NEWLYWED            : 신혼
    - MARRIED             : 부부
    """
)
public enum RelationshipStage {
    DATING,             // 연애중
    LIVING_TOGETHER,    // 동거중
    PREPARING_MARRIAGE, // 결혼 준비중
    NEWLYWED,           // 신혼
    MARRIED             // 부부
}
