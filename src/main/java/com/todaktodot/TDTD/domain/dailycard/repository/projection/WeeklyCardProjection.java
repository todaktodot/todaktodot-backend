package com.todaktodot.TDTD.domain.dailycard.repository.projection;

import java.time.LocalDate;

public interface WeeklyCardProjection {
    Long getCoupleCardId();
    Long getCoupleId();
    LocalDate getIssuedDate();
    String getSelectedYn();
    Long getCardId();
    String getCardTitle();
    String getMode();
    String getSubject();
    String getType();
    Integer getQuestionNo();
    String getQuestionType();
    String getQuestionCnts();
    String getAnswerReqYn();
    Integer getOptionNo();
    String getOptionCnts();
}
