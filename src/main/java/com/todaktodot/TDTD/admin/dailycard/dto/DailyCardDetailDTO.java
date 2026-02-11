package com.todaktodot.TDTD.admin.dailycard.dto;

import com.todaktodot.TDTD.domain.dailycard.repository.entity.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class DailyCardDetailDTO {

    private Long cardId;
    private CardMode mode;
    private CardSubject subject;
    private CardType type;
    private String cardTitle;
    private String situation;
    private String useYn;
    private LocalDateTime regDt;
    private LocalDateTime updDt;
    private List<QuestionDTO> questions;

    public static DailyCardDetailDTO from(DailyCardEntity entity) {
        return DailyCardDetailDTO.builder()
                .cardId(entity.getCardId())
                .mode(entity.getMode())
                .subject(entity.getSubject())
                .type(entity.getType())
                .cardTitle(entity.getCardTitle())
                .situation(entity.getSituation())
                .useYn(entity.getUseYn())
                .regDt(entity.getRegDt())
                .updDt(entity.getUpdDt())
                .questions(entity.getQuestions().stream()
                        .map(QuestionDTO::from)
                        .sorted((a, b) -> a.getQuestionNo().compareTo(b.getQuestionNo()))
                        .collect(Collectors.toList()))
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

    public boolean isActive() {
        return "Y".equals(useYn);
    }

    @Getter
    @Builder
    public static class QuestionDTO {
        private Long cardId;
        private Integer questionNo;
        private QuestionType questionType;
        private String answerReqYn;
        private String questionCnts;
        private List<OptionDTO> options;

        public static QuestionDTO from(DailyCardQuestionEntity entity) {
            return QuestionDTO.builder()
                    .cardId(entity.getCardId())
                    .questionNo(entity.getQuestionNo())
                    .questionType(entity.getQuestionType())
                    .answerReqYn(entity.getAnswerReqYn())
                    .questionCnts(entity.getQuestionCnts())
                    .options(entity.getOptions().stream()
                            .map(OptionDTO::from)
                            .sorted((a, b) -> a.getOptionNo().compareTo(b.getOptionNo()))
                            .collect(Collectors.toList()))
                    .build();
        }

        public String getQuestionTypeDisplayName() {
            return questionType == QuestionType.MULTIPLE_CHOICE ? "객관식" : "주관식";
        }

        public boolean isRequired() {
            return "Y".equals(answerReqYn);
        }
    }

    @Getter
    @Builder
    public static class OptionDTO {
        private Long cardId;
        private Integer questionNo;
        private Integer optionNo;
        private String optionCnts;

        public static OptionDTO from(DailyCardOptionEntity entity) {
            return OptionDTO.builder()
                    .cardId(entity.getCardId())
                    .questionNo(entity.getQuestionNo())
                    .optionNo(entity.getOptionNo())
                    .optionCnts(entity.getOptionCnts())
                    .build();
        }
    }
}
