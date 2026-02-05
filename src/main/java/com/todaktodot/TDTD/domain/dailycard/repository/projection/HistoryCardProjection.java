package com.todaktodot.TDTD.domain.dailycard.repository.projection;

import java.time.LocalDate;

public interface HistoryCardProjection {
    Long getCoupleCardId();
    LocalDate getIssuedDate();
    String getSelectedYn();
    Long getCardId();
    String getMode();
    String getSubject();
    String getType();
    Long getUser1Answered();
    Long getUser2Answered();
}
