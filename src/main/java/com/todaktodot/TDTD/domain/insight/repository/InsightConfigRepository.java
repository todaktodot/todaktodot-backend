package com.todaktodot.TDTD.domain.insight.repository;

import com.todaktodot.TDTD.domain.insight.repository.entity.InsightConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InsightConfigRepository extends JpaRepository<InsightConfig, Long> {

    Optional<InsightConfig> findTopByDelYnOrderByConfigIdDesc(String delYn);
}
