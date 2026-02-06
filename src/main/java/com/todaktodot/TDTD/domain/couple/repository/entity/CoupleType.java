package com.todaktodot.TDTD.domain.couple.repository.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description =
    """
    커플 유형
    - SOLO      : 혼자 둘러보기 (USER_ID_2가 NULL)
    - CONNECTED : 커플 연결 완료 (USER_ID_1, USER_ID_2 모두 존재)
    """
)
public enum CoupleType {
    SOLO,       // 혼자 둘러보기
    CONNECTED   // 커플 연결 완료
}
