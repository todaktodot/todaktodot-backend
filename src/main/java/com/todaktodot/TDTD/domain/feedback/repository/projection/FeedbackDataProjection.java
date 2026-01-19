package com.todaktodot.TDTD.domain.feedback.repository.projection;

/**
 * AI 피드백 생성에 필요한 데이터를 단일 쿼리로 조회하기 위한 Projection
 */
public interface FeedbackDataProjection {

    // 커플 정보
    Long getCoupleId();
    Long getUserId1();
    Long getUserId2();

    // 커플 카드 정보
    Long getCoupleCardId();
    Long getCardId();

    // 데일리카드 정보
    String getCardTitle();
    String getMode();
    String getSubject();
    String getType();

    // 질문 정보
    Integer getQuestionNo();
    String getAnswerReqYn();
    String getQuestionType();
    String getQuestionCnts();

    // 옵션 정보 (객관식인 경우)
    Integer getOptionNo();
    String getOptionCnts();

    // 유저 답변 정보
    // MULTIPLE_CHOICE: 해당 옵션 선택 시 'Y', 미선택 시 'N'
    // SUBJECTIVE: 실제 답변 내용
    String getUser1Answer();
    String getUser2Answer();
}
