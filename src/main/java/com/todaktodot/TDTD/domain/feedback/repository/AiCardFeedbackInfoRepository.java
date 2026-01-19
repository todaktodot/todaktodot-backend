package com.todaktodot.TDTD.domain.feedback.repository;

import com.todaktodot.TDTD.domain.feedback.repository.entity.AiCardFeedbackInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiCardFeedbackInfoRepository extends JpaRepository<AiCardFeedbackInfoEntity, Long> {
}
