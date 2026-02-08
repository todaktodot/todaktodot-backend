package com.todaktodot.TDTD.domain.insight.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GenerateInsightContext {
    private Long coupleId;
    private Long userId1;
    private Long userId2;
    private LocalDate startDt;
    private LocalDate endDt;
    private List<QuestionData> economyData;
    private List<QuestionData> lifestyleData;
    private List<QuestionData> loveData;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionData {
        private Long cardId;
        private String cardTitle;
        private String mode;
        private String subject;
        private String type;
        private OptionData optionData;
        private SubjectiveData subjectiveData;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionData {
        private int questionNo;
        private String questionType;
        private String questionCnt;
        private String optionAnswer1;
        private String optionAnswer2;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectiveData {
        private int questionNo;
        private String questionType;
        private String questionCnt;
        private String subjectiveAnswer1;
        private String subjectiveAnswer2;
    }

}
