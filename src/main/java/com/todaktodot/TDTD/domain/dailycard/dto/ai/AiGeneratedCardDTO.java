package com.todaktodot.TDTD.domain.dailycard.dto.ai;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * AI가 생성한 데일리카드 응답을 매핑하는 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class AiGeneratedCardDTO {

    private String cardTitle;
    private List<AiQuestionDTO> questions;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class AiQuestionDTO {
        private Integer questionNo;
        private String questionType;  // MULTIPLE_CHOICE or SUBJECTIVE
        private Boolean answerRequired;
        private String content;
        private List<AiOptionDTO> options;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class AiOptionDTO {
        private Integer optionNo;
        private String content;
    }
}