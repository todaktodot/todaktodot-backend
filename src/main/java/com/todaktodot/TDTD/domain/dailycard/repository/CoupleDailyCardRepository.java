package com.todaktodot.TDTD.domain.dailycard.repository;

import com.todaktodot.TDTD.domain.aireport.dto.response.SyncAnswerDTO;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardSubject;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CoupleDailyCardEntity;
import com.todaktodot.TDTD.domain.dailycard.repository.projection.HistoryCardProjection;
import com.todaktodot.TDTD.domain.dailycard.repository.projection.WeeklyCardProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CoupleDailyCardRepository extends JpaRepository<CoupleDailyCardEntity, Long> {

    Optional<CoupleDailyCardEntity> findByCoupleIdAndIssuedDateAndDelYn(
            Long coupleId, LocalDate issuedDate, String delYn);

    List<CoupleDailyCardEntity> findAllByCoupleIdAndIssuedDateAndDelYn(
            Long coupleId, LocalDate issuedDate, String delYn);

    boolean existsByCoupleIdAndIssuedDateAndDelYn(Long coupleId, LocalDate issuedDate, String delYn);

    long countByCoupleIdAndIssuedDateAndDelYn(Long coupleId, LocalDate issuedDate, String delYn);

    @EntityGraph(attributePaths = "dailyCard")
    Optional<CoupleDailyCardEntity> findTopByCoupleIdAndDelYnOrderByIssuedDateDesc(Long coupleId, String delYn);

    @EntityGraph(attributePaths = "dailyCard")
    List<CoupleDailyCardEntity> findAllByCoupleIdAndIssuedDateAndDelYnOrderByCoupleCardIdAsc(
            Long coupleId, LocalDate issuedDate, String delYn);

    List<CoupleDailyCardEntity> findAllByCoupleIdAndDelYnOrderByIssuedDateDescCoupleCardIdDesc(
            Long coupleId, String delYn);

    @Query("SELECT c FROM CoupleDailyCardEntity c " +
           "LEFT JOIN FETCH c.dailyCard " +
           "WHERE c.coupleCardId = :coupleCardId AND c.delYn = 'N'")
    Optional<CoupleDailyCardEntity> findByIdWithDailyCard(@Param("coupleCardId") Long coupleCardId);

    //한주 동안 제공된 데일리카드 중 주제에 따른 갯수
    @Query("SELECT count(cd.coupleCardId) FROM CoupleDailyCardEntity cd JOIN DailyCardEntity dc ON cd.cardId = dc.cardId " +
            "WHERE dc.subject = :subject " +
            "AND cd.regDt BETWEEN :startDT AND :endDT AND cd.delYn = :delYn")
    int findDailyCardBySubject(@Param("subject") CardSubject cardSubject,
                                                     @Param("startDT") LocalDateTime startDT,
                                                     @Param("endDT") LocalDateTime endDT,
                                                     @Param("delYn") String delYn);
    //커플ID와 데일리카드ID로 일치하는 커플 데일리카드 조회
    Optional<CoupleDailyCardEntity> findByCardIdAndCoupleId(Long cardId, Long coupleId);

    // 배정 내역 조회 (날짜 범위 필터)
    @Query("SELECT c FROM CoupleDailyCardEntity c " +
           "LEFT JOIN FETCH c.dailyCard " +
           "WHERE c.delYn = 'N' " +
           "AND c.issuedDate BETWEEN :startDate AND :endDate " +
           "ORDER BY c.regDt DESC, c.issuedDate DESC")
    List<CoupleDailyCardEntity> findAssignmentHistory(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // 주간 데일리카드 조회 (카드 마스터 + 질문 + 선택지 한 방 조회)
    @Query(value = """
        SELECT cdc.COUPLE_CARD_ID AS coupleCardId,
               cdc.COUPLE_ID AS coupleId,
               cdc.ISSUED_DATE AS issuedDate,
               cdc.SELECTED_YN AS selectedYn,
               dc.CARD_ID AS cardId,
               dc.CARD_TITLE AS cardTitle,
               dc.MODE AS mode,
               dc.SUBJECT AS subject,
               dc.TYPE AS type,
               dq.QUESTION_NO AS questionNo,
               dq.QUESTION_TYPE AS questionType,
               dq.QUESTION_CNTS AS questionCnts,
               dq.ANSWER_REQ_YN AS answerReqYn,
               do2.OPTION_NO AS optionNo,
               do2.OPTION_CNTS AS optionCnts
        FROM couple_daily_card cdc
            JOIN daily_card dc ON dc.CARD_ID = cdc.CARD_ID AND dc.DEL_YN = 'N'
            JOIN daily_card_question dq ON dq.CARD_ID = dc.CARD_ID AND dq.DEL_YN = 'N'
            LEFT JOIN daily_card_option do2
                ON do2.CARD_ID = dq.CARD_ID AND do2.QUESTION_NO = dq.QUESTION_NO AND do2.DEL_YN = 'N'
        WHERE cdc.COUPLE_ID = :coupleId
          AND cdc.ISSUED_DATE BETWEEN :startDate AND :endDate
          AND cdc.DEL_YN = 'N'
        ORDER BY cdc.ISSUED_DATE, cdc.COUPLE_CARD_ID, dq.QUESTION_NO, do2.OPTION_NO
        """, nativeQuery = true)
    List<WeeklyCardProjection> findWeeklyCardsWithDetails(
            @Param("coupleId") Long coupleId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // 히스토리 카드 리스트 조회 (카드 마스터 + user1/user2 답변 여부 한 방 조회)
    @Query(value = """
        SELECT cdc.COUPLE_CARD_ID AS coupleCardId,
               cdc.ISSUED_DATE AS issuedDate,
               cdc.SELECTED_YN AS selectedYn,
               dc.CARD_ID AS cardId,
               dc.MODE AS mode,
               dc.SUBJECT AS subject,
               dc.TYPE AS type,
               CASE WHEN a1.COUPLE_CARD_ID IS NOT NULL THEN true ELSE false END AS user1Answered,
               CASE WHEN a2.COUPLE_CARD_ID IS NOT NULL THEN true ELSE false END AS user2Answered
        FROM couple_daily_card cdc
            JOIN daily_card dc ON dc.CARD_ID = cdc.CARD_ID AND dc.DEL_YN = 'N'
            LEFT JOIN (SELECT DISTINCT COUPLE_CARD_ID FROM daily_card_user_answer
                       WHERE USER_ID = :userId1 AND DEL_YN = 'N') a1
                ON a1.COUPLE_CARD_ID = cdc.COUPLE_CARD_ID
            LEFT JOIN (SELECT DISTINCT COUPLE_CARD_ID FROM daily_card_user_answer
                       WHERE USER_ID = :userId2 AND DEL_YN = 'N') a2
                ON a2.COUPLE_CARD_ID = cdc.COUPLE_CARD_ID
        WHERE cdc.COUPLE_ID = :coupleId
          AND cdc.ISSUED_DATE BETWEEN :startDate AND :endDate
          AND cdc.DEL_YN = 'N'
        ORDER BY cdc.ISSUED_DATE ASC
        """, nativeQuery = true)
    List<HistoryCardProjection> findHistoryCards(
            @Param("coupleId") Long coupleId,
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // 최근 배정 내역 조회 (최신 N개)
    @Query("SELECT c FROM CoupleDailyCardEntity c " +
           "LEFT JOIN FETCH c.dailyCard " +
           "WHERE c.delYn = 'N' " +
           "ORDER BY c.regDt DESC, c.issuedDate DESC " +
           "LIMIT :limit")
    List<CoupleDailyCardEntity> findRecentAssignments(@Param("limit") int limit);
}
