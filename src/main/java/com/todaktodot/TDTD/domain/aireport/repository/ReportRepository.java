package com.todaktodot.TDTD.domain.aireport.repository;

import com.todaktodot.TDTD.domain.aireport.repository.entity.Report;
import com.todaktodot.TDTD.domain.couple.repository.entity.CoupleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ReportRepository extends JpaRepository<Report, Long> {
    /**
     * 한 주동안 생성된 AI리포트가 있는지 확인
     */
    boolean existsByCoupleEntityAndRegDtGreaterThanEqualAndRegDtLessThanAndDelYn(CoupleEntity coupleEntity, LocalDateTime startDT, LocalDateTime endDT, String delYn);
}
