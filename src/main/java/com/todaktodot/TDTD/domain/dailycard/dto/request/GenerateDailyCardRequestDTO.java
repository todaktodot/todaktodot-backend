package com.todaktodot.TDTD.domain.dailycard.dto.request;

import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardMode;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardSubject;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "데일리카드 AI 생성 요청")
public class GenerateDailyCardRequestDTO {

    @NotNull(message = "질문 모드는 필수입니다")
    @Schema(description = "질문 모드", example = "DESSERT")
    private CardMode mode;

    @NotNull(message = "질문 주제는 필수입니다")
    @Schema(description = "질문 주제", example = "LOVE")
    private CardSubject subject;

    @NotNull(message = "질문 유형은 필수입니다")
    @Schema(description = "질문 유형", example = "ROLEPLAY")
    private CardType type;
}
