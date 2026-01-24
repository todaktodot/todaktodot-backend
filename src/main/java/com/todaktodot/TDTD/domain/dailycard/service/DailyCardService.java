package com.todaktodot.TDTD.domain.dailycard.service;

import com.todaktodot.TDTD.domain.dailycard.dto.request.AssignCardRequestDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.request.GenerateDailyCardRequestDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.request.SubmitAnswerRequestDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.response.AssignBatchResponseDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.response.AssignCardResponseDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.response.GenerateDailyCardResponseDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.response.SubmitAnswerResponseDTO;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardMode;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardSubject;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardType;

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

    /**
     * 커플별 데일리카드 배정(배치/관리자 실행)
     *
     * @param startDate 배정 시작일
     * @param endDate 배정 종료일
     */
    AssignBatchResponseDTO assignDailyCardsForCouples(java.time.LocalDate startDate, java.time.LocalDate endDate);

    /**
     * AI 프롬프트 미리보기
     * 생성 전에 최종 프롬프트가 어떻게 구성되는지 확인
     *
     * @param mode 질문 모드
     * @param subject 질문 주제
     * @param type 질문 유형
     * @param situationCategory 상황 카테고리 (null이면 랜덤 선택)
     * @return 최종 프롬프트 문자열
     */
    String previewPrompt(CardMode mode, CardSubject subject, CardType type, String situationCategory);
}
