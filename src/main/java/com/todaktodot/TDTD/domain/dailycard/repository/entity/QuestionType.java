package com.todaktodot.TDTD.domain.dailycard.repository.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QuestionType {
    MULTIPLE_CHOICE("객관식"),
    SUBJECTIVE("주관식");

    private final String displayName;
}