package com.todaktodot.TDTD.domain.dailycard.repository;

import com.todaktodot.TDTD.domain.dailycard.repository.entity.DailyCardUserAnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
