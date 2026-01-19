package com.todaktodot.TDTD.domain.feedback.dto.reqeust;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "데일리카드 AI 피드백 생성 요청")
public class GenerateFeedbackRequestDTO {

    @NotNull(message = "커플 카드 ID는 필수입니다")
    @Schema(description = "커플 카드 ID", example = "1")
    private Long coupleCardId;

    @NotNull(message = "카드 ID는 필수입니다")
    @Schema(description = "데일리카드 ID", example = "1")
    private Long cardId;

    @NotNull(message = "발급 일자는 필수입니다")
    @Schema(description = "발급 일자", example = "2026-01-19")
    private LocalDate issuedDate;
}
