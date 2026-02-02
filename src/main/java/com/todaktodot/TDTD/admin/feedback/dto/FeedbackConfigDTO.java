package com.todaktodot.TDTD.admin.feedback.dto;

import com.todaktodot.TDTD.domain.feedback.repository.entity.AiFeedbackConfigEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class FeedbackConfigDTO {

    private Long configId;
    private Long promptId;
    private String aiModel;
    private BigDecimal temperature;
    private LocalDateTime regDt;

    public static FeedbackConfigDTO from(AiFeedbackConfigEntity entity) {
        return FeedbackConfigDTO.builder()
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
