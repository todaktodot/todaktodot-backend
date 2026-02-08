package com.todaktodot.TDTD.admin.couple.dto;

import com.todaktodot.TDTD.domain.insight.repository.entity.Insight;
import lombok.Getter;

/**
 * Admin 커플 상세 화면에서 인사이트 요약 정보를 표시하기 위한 DTO
 */
@Getter
public class InsightSummaryDTO {

    private final Long insightId;
    private final String summary;
    private final String economyPart;
    private final String lifestylePart;
    private final String lovePart;

    public InsightSummaryDTO(Long insightId, String summary, String economyPart,
                             String lifestylePart, String lovePart) {
        this.insightId = insightId;
        this.summary = summary;
        this.economyPart = economyPart;
        this.lifestylePart = lifestylePart;
        this.lovePart = lovePart;
    }

    public static InsightSummaryDTO from(Insight entity) {
        return new InsightSummaryDTO(
                entity.getId(),
                entity.getSummary(),
                entity.getEconomyPart(),
                entity.getLifestylePart(),
                entity.getLovePart()
        );
    }
}
