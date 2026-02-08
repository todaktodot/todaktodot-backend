package com.todaktodot.TDTD.admin.couple.service;

import com.todaktodot.TDTD.admin.couple.dto.CoupleDetailDTO;
import com.todaktodot.TDTD.admin.couple.dto.CoupleListDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface AdminCoupleService {

    Page<CoupleListDTO> getCouples(String delYn, Pageable pageable);

    CoupleDetailDTO getCouple(Long coupleId);

    long getTotalCount();

    long getActiveCount();

    long getInactiveCount();

    void deleteFeedback(Long coupleCardId);

    void deleteInsight(Long coupleId, Long reportId, LocalDate startDt, LocalDate endDt);
}
