package com.todaktodot.TDTD.domain.aireport.repository;

import com.todaktodot.TDTD.domain.aireport.repository.entity.Report;
import com.todaktodot.TDTD.domain.couple.repository.entity.CoupleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    /**
     * 한 주동안 생성된 AI리포트 조회
     */
    Optional<Report> findByCoupleEntityAndStrtDtAndEndDtAndDelYn(CoupleEntity coupleEntity, LocalDate strtDT, LocalDate endDT, String delYn);

    /**
     * Couple로 생성된 AI 리포트 목록 조회
     */
    List<Report> findAllByCoupleEntityAndDelYnOrderByRegDtDesc(CoupleEntity coupleEntity, String delYn);

    /**
     * 한주동안 생성된 리포트 조회 (배치용)
     */
    @Query("SELECT r FROM Report r WHERE r.strtDt = :startDt AND r.endDt = :endDt AND r.delYn = 'N'")
    List<Report> findCreatedReport(@Param("startDt") LocalDate startDt, @Param("endDt") LocalDate endDt);

}
