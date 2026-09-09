package com.todaktodot.TDTD.domain.vote.repository.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "사용자 작성 정지 상태 - SUSPENDED: 정지, RELEASED: 해제")
public enum SuspensionStatus {
    SUSPENDED("정지"),
    RELEASED("해제");

    private final String description;
}
