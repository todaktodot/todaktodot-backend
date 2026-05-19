package com.todaktodot.TDTD.domain.dailycard.dto.request;

import com.todaktodot.TDTD.domain.dailycard.repository.entity.EmojiType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SaveEmojiRequestDTO {

    @NotNull(message = "커플 카드 ID는 필수입니다")
    private Long coupleCardId;

    @NotNull(message = "이모지는 필수입니다")
    private EmojiType emojiType;
}
