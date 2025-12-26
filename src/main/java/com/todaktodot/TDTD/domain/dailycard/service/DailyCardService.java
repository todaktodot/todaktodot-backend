package com.todaktodot.TDTD.domain.dailycard.service;

import com.todaktodot.TDTD.domain.dailycard.dto.request.AssignCardRequestDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.request.GenerateDailyCardRequestDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.request.SubmitAnswerRequestDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.response.AssignCardResponseDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.response.GenerateDailyCardResponseDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.response.SubmitAnswerResponseDTO;

public interface DailyCardService {

    /**
     * AI를 사용하여 데일리카드 콘텐츠를 생성하고 DB에 저장
     *
     * @param requestDTO 질문 모드, 주제, 유형
     * @return 생성된 데일리카드 정보
     */
    GenerateDailyCardResponseDTO generateDailyCard(GenerateDailyCardRequestDTO requestDTO);

    /**
     * 데일리카드 단건 조회
     *
     * @param cardId 카드 ID
     * @return 데일리카드 정보 (질문, 옵션 포함)
     */
    GenerateDailyCardResponseDTO getDailyCard(Long cardId);

    /**
     * 데일리카드 답변 제출
     *
     * @param userId 답변하는 사용자 ID
     * @param requestDTO 답변 내용
     * @return 저장된 답변 정보
     */
    SubmitAnswerResponseDTO submitAnswer(Long userId, SubmitAnswerRequestDTO requestDTO);

    /**
     * 커플에게 데일리카드 할당
     *
     * @param userId 할당을 요청하는 사용자 ID
     * @param requestDTO 커플 ID, 카드 ID, 발급 일자
     * @return 할당된 커플 카드 정보
     */
    AssignCardResponseDTO assignCardToCouple(Long userId, AssignCardRequestDTO requestDTO);
}
