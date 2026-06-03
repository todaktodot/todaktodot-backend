package com.todaktodot.TDTD.domain.dailycard.repository.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "답변 이모지 반응 타입 - GOOD: 좋아요, HEART: 하트, SURPRISE: 놀람, CRY: 슬픔, ANGRY: 화남, POOP: 똥")
public enum EmojiType {
    GOOD("좋아요"),
    HEART("하트"),
    SURPRISE("놀람"),
    CRY("슬픔"),
    ANGRY("화남"),
    POOP("똥");

    private final String description;

    public static EmojiType from(String value) {
        if (value == null) {
            return null;
        }
        return EmojiType.valueOf(value);
    }
}
