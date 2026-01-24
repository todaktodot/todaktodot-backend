package com.todaktodot.TDTD.domain.dailycard.repository;

import com.todaktodot.TDTD.domain.aireport.dto.response.SyncAnswerDTO;
import com.todaktodot.TDTD.domain.couple.repository.entity.CoupleEntity;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardSubject;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.DailyCardUserAnswerEntity;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DailyCardUserAnswerRepository extends JpaRepository<DailyCardUserAnswerEntity, Long> {

    List<DailyCardUserAnswerEntity> findByCoupleCardIdAndUserIdAndDelYn(Long coupleCardId, Long userId, String delYn);

    Optional<DailyCardUserAnswerEntity> findByCoupleCardIdAndQuestionNoAndUserIdAndDelYn(
            Long coupleCardId, Integer questionNo, Long userId, String delYn);

    @Query("SELECT a FROM DailyCardUserAnswerEntity a " +
           "WHERE a.coupleCardId = :coupleCardId AND a.delYn = 'N'")
    List<DailyCardUserAnswerEntity> findAllByCoupleCardId(@Param("coupleCardId") Long coupleCardId);

    boolean existsByCoupleCardIdAndQuestionNoAndUserIdAndDelYn(
            Long coupleCardId, Integer questionNo, Long userId, String delYn);

    boolean existsByCoupleCardIdAndUserIdAndDelYn(Long coupleCardId, Long userId, String delYn);

    //한 주동안 응답한 데일리카드 유무 조회
    @Query("SELECT (count(d1) > 0) FROM DailyCardUserAnswerEntity d1 JOIN DailyCardUserAnswerEntity d2 ON d1.coupleCardId = d2.coupleCardId " +
            "AND d1.userId = :userId1 AND d2.userId = :userId2 AND d1.userId != d2.userId " +
            "WHERE d1.regDt BETWEEN :startDT AND :endDT AND d2.regDt BETWEEN :startDT AND :endDT AND d1.delYn = :delYn")
    boolean existsSameDailyCardAnswerInPeriod(@Param("userId1") Long userId1,
                                              @Param("userId2") Long userId2,
                                              @Param("startDT") LocalDateTime startDT,
                                              @Param("endDT") LocalDateTime endDT,
                                              @Param("delYn") String delYn);

    //한주 동안 응답한 데일리카드 중 주제에 따른 카드 수 및 응답
    @Query("SELECT dc.cardId AS cardId, d1.answerId AS answerId1, d2.answerId AS answerId2, d1.answerContent AS answerContent1, d2.answerContent AS answerContent2 FROM DailyCardUserAnswerEntity d1 " +
            "JOIN DailyCardUserAnswerEntity d2 ON d1.cardId = d2.cardId " +
            "AND d1.userId = :userId1 AND d2.userId = :userId2  AND d1.questionNo = d2.questionNo " +
            "JOIN DailyCardEntity dc ON d1.cardId = dc.cardId " +
            "JOIN DailyCardQuestionEntity dq ON dc.cardId = dq.cardId AND dq.questionNo = d1.questionNo " +
            "WHERE dc.subject = :subject AND dq.questionType = :questionType " +
            "AND d1.regDt BETWEEN :startDT AND :endDT AND d2.regDt BETWEEN :startDT AND :endDT AND d1.delYn = :delYn AND d2.delYn = :delYn")
    List<SyncAnswerDTO> findDailyCardAnswerBySubject(@Param("subject") CardSubject cardSubject,
                                                          @Param("userId1") Long userId1,
                                                          @Param("userId2") Long userId2,
                                                          @Param("questionType") QuestionType questionType,
                                                          @Param("startDT") LocalDateTime startDT,
                                                          @Param("endDT") LocalDateTime endDT,
                                                          @Param("delYn") String delYn);

    //모두 응답한 데일리카드 총 개수
    @Query("SELECT count(*) FROM DailyCardUserAnswerEntity d1 JOIN DailyCardUserAnswerEntity d2 ON d1.coupleCardId = d2.coupleCardId " +
            "AND d1.userId = :userId1 AND d2.userId = :userId2 AND d1.questionNo = d2.questionNo JOIN DailyCardEntity dc ON d1.cardId = dc.cardId " +
            "JOIN DailyCardQuestionEntity dq ON dc.cardId = dq.cardId AND dq.questionNo = d1.questionNo " +
            "WHERE dq.questionType = :questionType " +
            "AND d1.delYn = :delYn AND d2.delYn = :delYn")
    int findAllDailyCardAnswer(@Param("userId1") Long userId1,
                                                     @Param("userId2") Long userId2,
                                                     @Param("questionType") QuestionType questionType,
                                                     @Param("delYn") String delYn);
}
