package com.todaktodot.TDTD.domain.dailycard.service;

import com.todaktodot.TDTD.domain.dailycard.dto.request.GenerateDailyCardRequestDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.response.GenerateDailyCardResponseDTO;

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
}
