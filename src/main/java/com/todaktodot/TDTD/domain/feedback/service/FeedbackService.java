package com.todaktodot.TDTD.domain.feedback.service;

import com.todaktodot.TDTD.domain.feedback.dto.reqeust.GenerateFeedbackRequestDTO;
import com.todaktodot.TDTD.domain.feedback.dto.response.GenerateFeedbackResponseDTO;

public interface FeedbackService {

    /**
     * AI 피드백 생성 (두 명 답변 완료 후 호출)
     *
     * @param userId 요청 사용자 ID
     * @param requestDTO 피드백 생성 요청
     * @return 생성된 피드백
     */
    GenerateFeedbackResponseDTO generateFeedback(Long userId, GenerateFeedbackRequestDTO requestDTO);
}
