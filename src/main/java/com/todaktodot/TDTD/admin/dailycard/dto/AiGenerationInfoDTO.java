package com.todaktodot.TDTD.admin.dailycard.dto;

import com.todaktodot.TDTD.domain.dailycard.repository.entity.AiCardGenerationInfoEntity;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardMode;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardSubject;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 카드 생성 정보 DTO
 */
@Getter
@Builder
public class AiGenerationInfoDTO {

    private Long infoId;
    private Long cardId;
    private Long promptId;
    private String aiModel;
    private BigDecimal temperature;
    private CardMode mode;
    private CardSubject subject;
    private CardType type;
    private String situationCategory;
    private String finalPrompt;
    private String aiResponse;
    private LocalDateTime regDt;

    public static AiGenerationInfoDTO from(AiCardGenerationInfoEntity entity) {
        return AiGenerationInfoDTO.builder()
                .infoId(entity.getInfoId())
                .cardId(entity.getCardId())
                .promptId(entity.getPromptId())
                .aiModel(entity.getAiModel())
                .temperature(entity.getTemperature())
                .mode(entity.getMode())
                .subject(entity.getSubject())
                .type(entity.getType())
                .situationCategory(entity.getSituationCategory())
                .finalPrompt(entity.getFinalPrompt())
                .aiResponse(entity.getAiResponse())
                .regDt(entity.getRegDt())
                .build();
    }

    public String getModeDisplayName() {
        return mode != null ? mode.getDisplayName() : "";
    }

    public String getSubjectDisplayName() {
        return subject != null ? subject.getDisplayName() : "";
    }

    public String getTypeDisplayName() {
        return type != null ? type.getDisplayName() : "";
    }
}
