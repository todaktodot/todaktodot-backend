package com.todaktodot.TDTD.domain.dailycard.repository;

import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardMode;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardSubject;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardType;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.DailyCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DailyCardRepository extends JpaRepository<DailyCardEntity, Long> {

    List<DailyCardEntity> findByModeAndSubjectAndTypeAndUseYnAndDelYn(
            CardMode mode, CardSubject subject, CardType type, String useYn, String delYn);

    List<DailyCardEntity> findByModeAndSubjectAndUseYnAndDelYn(
            CardMode mode, CardSubject subject, String useYn, String delYn);
}