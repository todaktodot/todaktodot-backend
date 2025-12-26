package com.todaktodot.TDTD.domain.dailycard.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@Schema(description = "커플에게 데일리카드 할당 요청")
public class AssignCardRequestDTO {

    @NotNull(message = "커플 ID는 필수입니다")
    @Schema(description = "커플 ID", example = "1")
    private Long coupleId;

    @NotNull(message = "카드 ID는 필수입니다")
    @Schema(description = "할당할 데일리카드 ID", example = "1")
    private Long cardId;

    @NotNull(message = "발급 일자는 필수입니다")
    @Schema(description = "발급 일자", example = "2025-12-27")
    private LocalDate issuedDate;
}
