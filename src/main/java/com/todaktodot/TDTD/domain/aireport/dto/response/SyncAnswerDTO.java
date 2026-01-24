package com.todaktodot.TDTD.domain.aireport.dto.response;

public record SyncAnswerDTO(
    Long cardId,
    Long answerId1,
    Long answerId2,
    String answerContent1,
    String answerContent2
) {}
