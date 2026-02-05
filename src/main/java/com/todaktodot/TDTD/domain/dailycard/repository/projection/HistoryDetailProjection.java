package com.todaktodot.TDTD.domain.dailycard.repository.projection;

import java.time.LocalDate;

public interface HistoryDetailProjection {
    Long getCoupleCardId();
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
    String getUser1Answer();
    String getUser2Answer();
}
