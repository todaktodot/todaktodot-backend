package com.todaktodot.TDTD.domain.dailycard.repository.projection;

import java.time.LocalDate;

public interface GrassProjection {
    LocalDate getIssuedDate();
    Long getMeAnswered();
    Long getPartnerAnswered();
}
