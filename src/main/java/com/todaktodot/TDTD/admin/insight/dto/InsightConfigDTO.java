package com.todaktodot.TDTD.admin.insight.dto;

import com.todaktodot.TDTD.domain.insight.repository.entity.InsightConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class InsightConfigDTO {

    private Long configId;
    private Long promptId;
    private String aiModel;
    private BigDecimal temperature;
    private LocalDateTime regDt;

    public static InsightConfigDTO from(InsightConfig entity) {
        return InsightConfigDTO.builder()
                .configId(entity.getConfigId())
                .promptId(entity.getPromptId())
                .aiModel(entity.getAiModel())
                .temperature(entity.getTemperature())
                .regDt(entity.getRegDt())
                .build();
    }

    @Getter
    @Setter
    public static class SaveRequest {
        private Long promptId;
        private String aiModel;
        private BigDecimal temperature;
    }
}
