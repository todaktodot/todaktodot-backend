package com.todaktodot.TDTD.domain.dailycard.repository;

import com.todaktodot.TDTD.domain.dailycard.repository.entity.DailyCardOptionEntity;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.DailyCardOptionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DailyCardOptionRepository extends JpaRepository<DailyCardOptionEntity, DailyCardOptionId> {

    List<DailyCardOptionEntity> findByCardIdAndQuestionNoAndDelYn(Long cardId, Integer questionNo, String delYn);
}