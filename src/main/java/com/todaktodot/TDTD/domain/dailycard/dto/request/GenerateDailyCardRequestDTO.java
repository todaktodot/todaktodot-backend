package com.todaktodot.TDTD.domain.dailycard.dto.request;

import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardMode;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardSubject;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
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

    @Schema(description = "사용할 프롬프트 ID")
    private Long promptId;

    @Schema(description = "상황 카테고리 (선택사항, null이면 랜덤 선택)")
    private String situationCategory;

    @Schema(description = "AI 모델 (선택사항, 기본값: gpt-5.4)", example = "gpt-5.4")
    private String aiModel = "gpt-5.4";

    @Schema(description = "AI 온도 설정 (0.0~2.0, 기본값: 0.8)", example = "0.8")
    private Double temperature = 0.8;
}
