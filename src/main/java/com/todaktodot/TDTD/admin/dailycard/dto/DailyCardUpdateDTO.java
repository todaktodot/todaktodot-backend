package com.todaktodot.TDTD.admin.dailycard.dto;

import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardMode;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardSubject;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardType;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DailyCardUpdateDTO {

    @NotNull(message = "카드 ID는 필수입니다")
    private Long cardId;

    @NotNull(message = "모드는 필수입니다")
    private CardMode mode;

    @NotNull(message = "주제는 필수입니다")
    private CardSubject subject;

    @NotNull(message = "유형은 필수입니다")
    private CardType type;

    @NotBlank(message = "카드 제목은 필수입니다")
    private String cardTitle;

    private String useYn = "Y";

    private List<QuestionUpdateDTO> questions;

    @Getter
    @Setter
    public static class QuestionUpdateDTO {
        private Integer questionNo;
        private QuestionType questionType;
        private String answerReqYn;
        private String questionCnts;
        private List<OptionUpdateDTO> options;
    }

    @Getter
    @Setter
    public static class OptionUpdateDTO {
        private Integer optionNo;
        private String optionCnts;
    }
}
