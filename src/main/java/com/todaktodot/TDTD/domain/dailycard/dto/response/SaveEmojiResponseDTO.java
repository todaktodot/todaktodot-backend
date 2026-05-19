package com.todaktodot.TDTD.domain.dailycard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SaveEmojiResponseDTO {

    private long userId;

    private long coupleCardId;

    private String emojiType;

    private int questionNo;

    private LocalDateTime updateDt;

}
