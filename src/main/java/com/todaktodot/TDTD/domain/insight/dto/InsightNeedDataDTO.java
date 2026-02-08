package com.todaktodot.TDTD.domain.insight.dto;

public record InsightNeedDataDTO(
    //데일리 카드 ID
    Long cardId,
    Long userId1,
    Long userId2,
    // 데일리카드 정보
    String cardTitle,
    String mode,
    String subject,
    String type,

    // 질문 정보
    Integer questionNo,
    String questionType,
    String questionCnts,

    // 유저 답변
    String optionAnswer1,
    String optionAnswer2,
    String subjectiveAnswer1,
    String subjectiveAnswer2
) {}
