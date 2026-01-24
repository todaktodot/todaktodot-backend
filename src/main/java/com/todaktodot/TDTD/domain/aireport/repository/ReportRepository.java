package com.todaktodot.TDTD.domain.aireport.repository;

import com.todaktodot.TDTD.domain.aireport.repository.entity.Report;
import com.todaktodot.TDTD.domain.couple.repository.entity.CoupleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    /**
     * 한 주동안 생성된 AI리포트 조회
     */
    Optional<Report> findByCoupleEntityAndRegDtGreaterThanEqualAndRegDtLessThanAndDelYn(CoupleEntity coupleEntity, LocalDateTime startDT, LocalDateTime endDT, String delYn);

    /**
     * Couple로 생성된 AI 리포트 목록 조회
     */
    List<Report> findAllByCoupleEntityAndDelYnOrderByRegDtDesc(CoupleEntity coupleEntity, String delYn);

}
