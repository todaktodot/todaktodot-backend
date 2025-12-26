package com.todaktodot.TDTD.domain.dailycard.repository;

import com.todaktodot.TDTD.domain.dailycard.repository.entity.DailyCardQuestionEntity;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.DailyCardQuestionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DailyCardQuestionRepository extends JpaRepository<DailyCardQuestionEntity, DailyCardQuestionId> {

    List<DailyCardQuestionEntity> findByCardIdAndDelYn(Long cardId, String delYn);
}