package com.todaktodot.TDTD.domain.dailycard.dto.response;

import com.todaktodot.TDTD.domain.dailycard.repository.entity.CoupleDailyCardEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "커플에게 데일리카드 할당 응답")
public class AssignCardResponseDTO {

    @Schema(description = "커플 카드 ID", example = "1")
    private Long coupleCardId;

    @Schema(description = "커플 ID", example = "1")
    private Long coupleId;

    @Schema(description = "데일리카드 ID", example = "1")
    private Long cardId;

    @Schema(description = "발급 일자", example = "2025-12-27")
    private LocalDate issuedDate;

    @Schema(description = "할당 일시")
    private LocalDateTime assignedAt;

    public static AssignCardResponseDTO from(CoupleDailyCardEntity entity) {
        return AssignCardResponseDTO.builder()
                .coupleCardId(entity.getCoupleCardId())
                .coupleId(entity.getCoupleId())
                .cardId(entity.getCardId())
                .issuedDate(entity.getIssuedDate())
                .assignedAt(entity.getRegDt())
                .build();
    }
}
