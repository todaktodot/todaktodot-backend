package com.todaktodot.TDTD.domain.dailycard.repository;

import com.todaktodot.TDTD.domain.dailycard.repository.entity.DailyCardAnswerReactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DailyCardReactionRepository extends JpaRepository<DailyCardAnswerReactionEntity, Long> {
    Optional<DailyCardAnswerReactionEntity> findByReactorUserIdAndAnswerIdAndDelYn(long reactorUserId, Long answerId, String delYn);
}
