package com.todaktodot.TDTD.domain.dailycard.repository;

import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardMode;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardSubject;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardType;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.DailyCardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DailyCardRepository extends JpaRepository<DailyCardEntity, Long>, JpaSpecificationExecutor<DailyCardEntity> {

    @Query("SELECT DISTINCT d FROM DailyCardEntity d " +
           "LEFT JOIN FETCH d.questions q " +
           "LEFT JOIN FETCH q.options " +
           "WHERE d.cardId = :cardId")
    Optional<DailyCardEntity> findByIdWithQuestionsAndOptions(@Param("cardId") Long cardId);

    @Query("SELECT DISTINCT d FROM DailyCardEntity d " +
           "LEFT JOIN FETCH d.questions q " +
           "LEFT JOIN FETCH q.options " +
           "WHERE d.cardId IN :cardIds")
    List<DailyCardEntity> findAllByIdWithQuestionsAndOptions(@Param("cardIds") List<Long> cardIds);

    List<DailyCardEntity> findByModeAndSubjectAndTypeAndUseYnAndDelYn(
            CardMode mode, CardSubject subject, CardType type, String useYn, String delYn);

    List<DailyCardEntity> findByModeAndSubjectAndUseYnAndDelYn(
            CardMode mode, CardSubject subject, String useYn, String delYn);

    @Query("SELECT DISTINCT d FROM DailyCardEntity d " +
           "LEFT JOIN FETCH d.questions q " +
           "LEFT JOIN FETCH q.options " +
           "WHERE d.delYn = 'N' " +
           "ORDER BY d.regDt DESC")
    List<DailyCardEntity> findAllWithQuestionsAndOptions();

    @Query("SELECT d FROM DailyCardEntity d WHERE d.delYn = 'N' ORDER BY d.regDt DESC")
    Page<DailyCardEntity> findAllActiveCards(Pageable pageable);

    /**
     * 목록 조회용
     */
    @Query(value = """
            SELECT dc.card_id, dc.mode, dc.subject, dc.type, dc.card_title,
                   (SELECT COUNT(*) FROM daily_card_question dcq WHERE dcq.card_id = dc.card_id) AS question_cnt,
                   dc.use_yn, dc.reg_dt
            FROM daily_card dc
            WHERE dc.del_yn = 'N'
            ORDER BY dc.reg_dt DESC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<Object[]> findAllForListNative(@Param("limit") int limit, @Param("offset") int offset);

    /**
     * 통계 조회
     */
    @Query(value = """
            SELECT
                COUNT(*) AS total_count,
                SUM(CASE WHEN use_yn = 'Y' THEN 1 ELSE 0 END) AS active_count,
                SUM(CASE WHEN use_yn = 'N' THEN 1 ELSE 0 END) AS inactive_count
            FROM daily_card
            WHERE del_yn = 'N'
            """, nativeQuery = true)
    List<Object[]> getCardStatistics();

    long countByDelYn(String delYn);

    long countByUseYnAndDelYn(String useYn, String delYn);

    @Modifying
    @Query("UPDATE DailyCardEntity d SET d.delYn = 'Y', d.updrId = :updrId WHERE d.cardId = :cardId")
    void softDelete(@Param("cardId") Long cardId, @Param("updrId") Long updrId);

    @Modifying
    @Query("UPDATE DailyCardEntity d SET d.useYn = :useYn, d.updrId = :updrId WHERE d.cardId = :cardId")
    void updateUseYn(@Param("cardId") Long cardId, @Param("useYn") String useYn, @Param("updrId") Long updrId);
}