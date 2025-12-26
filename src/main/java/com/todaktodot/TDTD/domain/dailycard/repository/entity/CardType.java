package com.todaktodot.TDTD.domain.dailycard.repository.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CardType {
    ROLEPLAY("상황극", 5),
    BALANCE("밸런스게임", 2);

    private final String displayName;
    private final int optionCount;
}