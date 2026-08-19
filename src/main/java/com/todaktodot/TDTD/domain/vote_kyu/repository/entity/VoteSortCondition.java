package com.todaktodot.TDTD.domain.vote_kyu.repository.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VoteSortCondition {
    LATEST("최신순"),
    POPULAR("인기순");

    private final String displayName;
}
