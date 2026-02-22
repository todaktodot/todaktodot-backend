package com.todaktodot.TDTD.domain.insight.repository;

import com.todaktodot.TDTD.domain.insight.repository.entity.InsightConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InsightConfigRepository extends JpaRepository<InsightConfig, Long> {

    Optional<InsightConfig> findTopByDelYnOrderByConfigIdDesc(String delYn);

    List<InsightConfig> findAllByPromptIdOrderByConfigIdDesc(Long promptId);
}
