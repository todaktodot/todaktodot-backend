package com.todaktodot.TDTD.domain.feedback.service;

import com.todaktodot.TDTD.domain.feedback.dto.reqeust.GenerateFeedbackRequestDTO;
import com.todaktodot.TDTD.domain.feedback.dto.response.FeedbackResponseDTO;

public interface FeedbackService {

    /**
     * AI 피드백 생성 (두 명 답변 완료 후 호출)
     *
     * @param userId 요청 사용자 ID
     * @param requestDTO 피드백 생성 요청
     * @return 생성된 피드백
     */
    FeedbackResponseDTO generateFeedback(Long userId, GenerateFeedbackRequestDTO requestDTO);

    /**
     * 커플 데일리카드의 AI 피드백 상태/결과 단건 조회
     *
     * @param userId 요청 사용자 ID
     * @param coupleCardId 커플 데일리카드 ID
     * @return 피드백 생성 상태와 피드백 본문
     */
    FeedbackResponseDTO getFeedback(Long userId, Long coupleCardId);
}
