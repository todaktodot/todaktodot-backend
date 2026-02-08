package com.todaktodot.TDTD.domain.insight.dto;

import com.todaktodot.TDTD.domain.feedback.dto.response.GenerateFeedbackResponseDTO;
import com.todaktodot.TDTD.domain.feedback.repository.entity.DailyCardFeedbackEntity;
import com.todaktodot.TDTD.domain.insight.repository.entity.Insight;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 생성된 AI인사이트 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GenerateInsightResponseDTO {
    @Schema(description = "커플ID")
    private Long coupleId;
    @Schema(description = "인사이트ID")
    private Long insightId;
    @Schema(description = "인사이트 생성 시작날짜")
    private LocalDate startDt;
    @Schema(description = "인사이트 생성 종료날짜")
    private LocalDate endDt;
    @Schema(description = "인사이트 요약")
    private String summary;
    @Schema(description = "인사이트 경제관 부분")
    private String economyPart;
    @Schema(description = "인사이트 생활관 부분")
    private String loveStylePart;
    @Schema(description = "인사이트 연애관 부분")
    private String lifeStylePart;

    public static GenerateInsightResponseDTO from(Insight insight) {
        return new GenerateInsightResponseDTO(
                insight.getCoupleId(),
                insight.getId(),
                insight.getStartDt(),
                insight.getEndDt(),
                insight.getSummary(),
                insight.getEconomyPart(),
                insight.getLifestylePart(),
                insight.getLovePart()
        );
    }
}
