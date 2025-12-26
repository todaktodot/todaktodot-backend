package com.todaktodot.TDTD.domain.dailycard.dto.response;

import com.todaktodot.TDTD.domain.dailycard.dto.ai.AiGeneratedCardDTO;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
@Builder
@Schema(description = "데일리카드 AI 생성 응답")
public class GenerateDailyCardResponseDTO {

    @Schema(description = "생성된 카드 ID")
    private Long cardId;

    @Schema(description = "질문 모드")
    private CardMode mode;

    @Schema(description = "질문 주제")
    private CardSubject subject;

    @Schema(description = "질문 유형")
    private CardType type;

    @Schema(description = "메인 질문/상황 설명")
    private String cardTitle;

    @Schema(description = "질문 목록")
    private List<QuestionDTO> questions;

    @Getter
    @Builder
    public static class QuestionDTO {
        private Integer questionNo;
        private String questionType;
        private Boolean answerRequired;
        private String content;
        private List<OptionDTO> options;
    }

    @Getter
    @Builder
    public static class OptionDTO {
        private Integer optionNo;
        private String content;
    }

    public static GenerateDailyCardResponseDTO from(DailyCardEntity entity) {
        return GenerateDailyCardResponseDTO.builder()
                .cardId(entity.getCardId())
                .mode(entity.getMode())
                .subject(entity.getSubject())
                .type(entity.getType())
                .cardTitle(entity.getCardTitle())
                .questions(entity.getQuestions().stream()
                        .map(q -> QuestionDTO.builder()
                                .questionNo(q.getQuestionNo())
                                .questionType(q.getQuestionType().name())
                                .answerRequired("Y".equals(q.getAnswerReqYn()))
                                .content(q.getQuestionCnts())
                                .options(q.getOptions().stream()
                                        .map(o -> OptionDTO.builder()
                                                .optionNo(o.getOptionNo())
                                                .content(o.getOptionCnts())
                                                .build())
                                        .toList())
                                .build())
                        .toList())
                .build();
    }

    public static GenerateDailyCardResponseDTO of(DailyCardEntity entity, AiGeneratedCardDTO aiResponse) {
        return GenerateDailyCardResponseDTO.builder()
                .cardId(entity.getCardId())
                .mode(entity.getMode())
                .subject(entity.getSubject())
                .type(entity.getType())
                .cardTitle(aiResponse.getCardTitle())
                .questions(aiResponse.getQuestions().stream()
                        .map(q -> QuestionDTO.builder()
                                .questionNo(q.getQuestionNo())
                                .questionType(q.getQuestionType())
                                .answerRequired(q.getAnswerRequired())
                                .content(q.getContent())
                                .options(q.getOptions() != null
                                        ? q.getOptions().stream()
                                                .map(o -> OptionDTO.builder()
                                                        .optionNo(o.getOptionNo())
                                                        .content(o.getContent())
                                                        .build())
                                                .toList()
                                        : Collections.emptyList())
                                .build())
                        .toList())
                .build();
    }
}
