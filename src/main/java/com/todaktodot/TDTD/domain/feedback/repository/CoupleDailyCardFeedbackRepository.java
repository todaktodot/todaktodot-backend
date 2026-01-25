package com.todaktodot.TDTD.domain.feedback.repository;

import com.todaktodot.TDTD.domain.feedback.repository.entity.CoupleDailyCardFeedbackEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoupleDailyCardFeedbackRepository extends JpaRepository<CoupleDailyCardFeedbackEntity, Long> {

    Optional<CoupleDailyCardFeedbackEntity> findByCoupleCardIdAndDelYn(Long coupleCardId, String delYn);

    List<CoupleDailyCardFeedbackEntity> findAllByCoupleCardIdInAndDelYn(List<Long> coupleCardIds, String delYn);
}
