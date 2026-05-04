package com.todaktodot.TDTD.domain.insight.repository;

import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardSubject;
import com.todaktodot.TDTD.domain.insight.dto.InsightNeedDataDTO;
import com.todaktodot.TDTD.domain.insight.repository.entity.Insight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.method.P;

import java.time.LocalDate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InsightRepository extends JpaRepository<Insight, Long> {
    /**
     * 한 주동안 생성된 인사이트 조회
     */
    Optional<Insight> findByCoupleIdAndStartDtAndEndDtAndDelYn(Long coupleId, LocalDate startDt, LocalDate endDt, String delYn);

    /**
     * 주제별 한 주동안 유저 모두 응답한 데일리카드의 정보 및 답변 정보 조회
     */
    @Query(nativeQuery = true, value = """
            SELECT
            	d1.card_id AS cardId
            	,d1.user_id AS userId1
            	,d2.user_id AS userId2
            	,dc.card_title
            	,dc.mode
            	,dc.subject
            	,dc.type
            	,dq.question_no
            	,dq.question_type
            	,dq.question_cnts
            	,dco1.option_cnts AS optionAnswer1
            	,dco2.option_cnts AS optionAnswer2
            	,CASE WHEN dq.question_type = 'MULTIPLE_CHOICE' THEN NULL
            			ELSE d1.answer_content
            	 		END AS subjectiveAnswer1
            	,CASE WHEN dq.question_type = 'MULTIPLE_CHOICE' THEN NULL
            			ELSE d2.answer_content
            			END AS subjectiveAnswer2
            FROM daily_card_user_answer d1
            JOIN daily_card_user_answer d2
            	ON d1.card_id = d2.card_id
            	AND d1.user_id = :userId1
            	AND d2.user_id = :userId2
            	AND d1.question_no = d2.question_no
            	AND d1.del_yn = 'N'
            	AND d2.del_yn = 'N'
            JOIN couple_daily_card cdc
            	ON d1.couple_card_id = cdc.couple_card_id
            	AND cdc.couple_id = :coupleId
            JOIN daily_card dc
            	ON d1.card_id = dc.card_id
            	AND dc.del_yn = 'N'
            JOIN daily_card_question dq
            	ON dc.card_id = dq.card_id
            	AND dq.question_no = d1.question_no
            	AND dq.del_yn = 'N'
            LEFT JOIN daily_card_option dco1
            	ON dc.card_id = dco1.card_id
            	AND dco1.option_no = d1.answer_content
            	AND dco1.del_yn = 'N'
            LEFT JOIN daily_card_option dco2
            	ON dc.card_id = dco2.card_id
            	AND dco2.option_no = d2.answer_content
            	AND dco2.del_yn = 'N'
            WHERE dc.subject = :subject
            AND d1.reg_dt BETWEEN :startDt AND :endDt AND d2.reg_dt BETWEEN :startDt AND :endDt
            """)
    List<InsightNeedDataDTO> findInsightDataByCouple(@Param("subject") String cardSubject,
                                                     @Param("coupleId") Long coupleId,
                                                     @Param("userId1") Long userId1,
                                                     @Param("userId2") Long userId2,
                                                     @Param("startDt") LocalDateTime startDt,
                                                     @Param("endDt") LocalDateTime endDt);

}
