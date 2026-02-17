package com.todaktodot.TDTD.domain.dailycard.service;

import com.todaktodot.TDTD.domain.dailycard.dto.request.AssignCardRequestDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.request.GenerateDailyCardRequestDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.request.SelectCardTypeRequestDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.request.SubmitAnswerRequestDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.response.AssignBatchResponseDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.response.AssignCardResponseDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.response.AssignMyCardResponseDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.response.GenerateDailyCardResponseDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.response.HistoryCardResponseDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.response.HistoryDetailResponseDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.response.SelectCardTypeResponseDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.response.SubmitAnswerResponseDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.response.WeeklyCardResponseDTO;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardMode;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardSubject;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardType;

import java.time.LocalDate;

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

    /**
     * 주간 배정 데일리카드 조회 (카드 + 질문 + 선택지 포함)
     *
     * @param userId 요청 사용자 ID
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return 주간 데일리카드 목록
     */
    WeeklyCardResponseDTO getWeeklyCards(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 내 커플 데일리카드 실시간 배정
     * 프론트엔드가 원하는 시점에 호출하여 날짜 범위에 대해 배정.
     * 이미 배정된 날짜는 자동 스킵
     *
     * @param userId 요청 사용자 ID
     * @param startDate 배정 시작일
     * @param endDate 배정 종료일
     * @return 배정 결과 (배정 수, 스킵 수)
     */
    AssignMyCardResponseDTO assignMyDailyCards(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 데일리카드 유형 선택 및 미선택 카드 soft delete
     * 당일 배정된 2개 카드 중 선택한 유형의 카드를 활성화하고, 나머지를 삭제 처리
     *
     * @param userId 선택하는 사용자 ID
     * @param request 발급일자 + 선택 유형
     * @return 선택된 카드 정보
     */
    SelectCardTypeResponseDTO selectCardType(Long userId, SelectCardTypeRequestDTO request);

    /**
     * 히스토리 카드 리스트 조회
     * 날짜 범위 내 배정된 데일리카드를 일자별로 조회.
     * 유형 선택 완료 시 전체 정보, 미선택 시 모드/주제만 노출
     *
     * @param userId 요청 사용자 ID
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return 히스토리 카드 리스트
     */
    HistoryCardResponseDTO getHistoryCards(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 히스토리 카드 상세 리스트 조회
     * 날짜 범위 내 배정된 데일리카드를 일자별로 조회하되,
     * 선택 완료 카드는 질문/선택지/답변/AI 피드백까지 포함.
     * 앱 메인 진입 시 한 번의 호출로 모든 데이터를 로드하기 위한 API.
     *
     * @param userId 요청 사용자 ID
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return 히스토리 카드 상세 리스트
     */
    HistoryDetailResponseDTO getHistoryDetailCards(Long userId, LocalDate startDate, LocalDate endDate);

    HistoryDetailResponseDTO getHistoryDetailCard(Long userId, Long coupleCardId);
}
