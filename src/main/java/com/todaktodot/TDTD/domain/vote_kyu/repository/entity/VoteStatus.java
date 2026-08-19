package com.todaktodot.TDTD.domain.vote_kyu.repository.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VoteStatus {
    ACTIVE("진행중"),
    CLOSED("마감");

    private final String displayName;
}
