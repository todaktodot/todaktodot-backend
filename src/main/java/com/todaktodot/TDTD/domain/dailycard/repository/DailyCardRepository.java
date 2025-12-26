package com.todaktodot.TDTD.domain.dailycard.repository;

import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardMode;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardSubject;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardType;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.DailyCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DailyCardRepository extends JpaRepository<DailyCardEntity, Long> {

    /**
     * 데일리카드를 질문, 옵션과 함께 한 번의 쿼리로 조회
     */
    @Query("SELECT DISTINCT d FROM DailyCardEntity d " +
           "LEFT JOIN FETCH d.questions q " +
           "LEFT JOIN FETCH q.options " +
           "WHERE d.cardId = :cardId")
    Optional<DailyCardEntity> findByIdWithQuestionsAndOptions(@Param("cardId") Long cardId);

    List<DailyCardEntity> findByModeAndSubjectAndTypeAndUseYnAndDelYn(
            CardMode mode, CardSubject subject, CardType type, String useYn, String delYn);

    List<DailyCardEntity> findByModeAndSubjectAndUseYnAndDelYn(
            CardMode mode, CardSubject subject, String useYn, String delYn);
}