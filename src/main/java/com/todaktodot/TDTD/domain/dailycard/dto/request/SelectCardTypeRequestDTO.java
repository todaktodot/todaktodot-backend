package com.todaktodot.TDTD.domain.dailycard.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SelectCardTypeRequestDTO {

    @NotNull(message = "커플 카드 ID는 필수입니다")
    private Long coupleCardId;
}
