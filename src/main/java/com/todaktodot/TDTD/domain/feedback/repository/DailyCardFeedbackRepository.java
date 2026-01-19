package com.todaktodot.TDTD.domain.feedback.repository;

import com.todaktodot.TDTD.domain.feedback.repository.entity.DailyCardFeedbackEntity;
import com.todaktodot.TDTD.domain.feedback.repository.projection.FeedbackDataProjection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyCardFeedbackRepository extends JpaRepository<DailyCardFeedbackEntity, Long> {

    Optional<DailyCardFeedbackEntity> findByCardIdAndChoiceCombinationHashAndHasSubjectiveAndDelYn(
            Long cardId, String choiceCombinationHash, String hasSubjective, String delYn);

    /**
     * AI 피드백 생성에 필요한 모든 데이터를 단일 쿼리로 조회
     * - 커플 정보, 카드 정보, 질문/옵션 정보, 두 유저의 답변을 한 번에 조회
     * - 요청자가 해당 커플에 속하는지 검증 포함
     *
     * @param cardId 데일리카드 ID
     * @param coupleCardId 커플 카드 ID
     * @param issuedDate 발급 일자
     * @param requestUserId 요청자 ID (권한 검증용)
     * @return 피드백 생성에 필요한 데이터 목록
     */
    @Query(nativeQuery = true, value = """
            SELECT c.couple_id AS coupleId
                 , c.user_id_1 AS userId1
                 , c.user_id_2 AS userId2
                 , cdc.couple_card_id AS coupleCardId
                 , cdc.card_id AS cardId
                 , dc.card_title AS cardTitle
                 , dc.mode AS mode
                 , dc.subject AS subject
                 , dc.type AS type
                 , dcq.question_no AS questionNo
                 , dcq.answer_req_yn AS answerReqYn
                 , dcq.question_type AS questionType
                 , dcq.question_cnts AS questionCnts
                 , dco.option_no AS optionNo
                 , dco.option_cnts AS optionCnts
                 , CASE WHEN dcq.question_type = 'MULTIPLE_CHOICE' AND dco.option_no = dcua1.answer_content THEN 'Y'
                        WHEN dcq.question_type = 'SUBJECTIVE' THEN dcua1.answer_content
                        ELSE 'N' END AS user1Answer
                 , CASE WHEN dcq.question_type = 'MULTIPLE_CHOICE' AND dco.option_no = dcua2.answer_content THEN 'Y'
                        WHEN dcq.question_type = 'SUBJECTIVE' THEN dcua2.answer_content
                        ELSE 'N' END AS user2Answer
              FROM couple c
              JOIN couple_daily_card cdc
                ON cdc.couple_id = c.couple_id
               AND cdc.del_yn = 'N'
               AND cdc.card_id = :cardId
               AND cdc.couple_card_id = :coupleCardId
               AND cdc.issued_date = :issuedDate
               AND cdc.selected_yn = 'Y'
              JOIN daily_card dc
                ON dc.card_id = cdc.card_id
               AND dc.del_yn = 'N'
              JOIN daily_card_question dcq
                ON dcq.card_id = dc.card_id
               AND dcq.del_yn = 'N'
         LEFT JOIN daily_card_option dco
                ON dco.card_id = dcq.card_id
               AND dco.question_no = dcq.question_no
               AND dco.del_yn = 'N'
         LEFT JOIN daily_card_user_answer dcua1
                ON dcua1.couple_card_id = cdc.couple_card_id
               AND dcua1.card_id = dc.card_id
               AND dcua1.question_no = dcq.question_no
               AND dcua1.user_id = c.user_id_1
         LEFT JOIN daily_card_user_answer dcua2
                ON dcua2.couple_card_id = cdc.couple_card_id
               AND dcua2.card_id = dc.card_id
               AND dcua2.question_no = dcq.question_no
               AND dcua2.user_id = c.user_id_2
             WHERE (c.user_id_1 = :requestUserId OR c.user_id_2 = :requestUserId)
               AND c.del_yn = 'N'
             ORDER BY dcq.question_no, dco.option_no
            """)
    List<FeedbackDataProjection> findFeedbackDataByCardAndUser(
            @Param("cardId") Long cardId,
            @Param("coupleCardId") Long coupleCardId,
            @Param("issuedDate") LocalDate issuedDate,
            @Param("requestUserId") Long requestUserId);
}
