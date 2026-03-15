package com.todaktodot.TDTD.domain.feedback.event;

import java.time.LocalDate;

public record BothAnswersCompletedEvent(
        Long userId,
        Long coupleCardId,
        Long cardId,
        LocalDate issuedDate
) {}
