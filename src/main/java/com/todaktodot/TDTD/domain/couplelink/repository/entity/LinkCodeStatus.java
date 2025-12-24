package com.todaktodot.TDTD.domain.couplelink.repository.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description =
    """
    커플 연결 코드
    - ISSUED  : 발급됨
    - EXPIRED : 만료됨
    - LINKED  : 연결됨
    """
)
public enum LinkCodeStatus {
    ISSUED,   // 발급됨
    EXPIRED,  // 만료됨
    LINKED    // 연결됨
}