package com.todaktodot.TDTD.domain.dailycard.repository.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CardMode {
    DESSERT("디저트", "가벼운 취향/선호 질문"),
    COFFEE("커피", "경험/방식에 대한 질문"),
    WHISKEY("위스키", "가치관/철학에 대한 질문");

    private final String displayName;
    private final String description;
}