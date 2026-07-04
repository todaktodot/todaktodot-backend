package com.todaktodot.TDTD.domain.dailycard.repository;

import com.todaktodot.TDTD.domain.dailycard.repository.entity.CoupleDailyCardEntity;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.DailyCardShareLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyCardShareLinkReposittory extends JpaRepository<DailyCardShareLink, Long> {
    Optional<DailyCardShareLink> findByShareTokenAndDelYn(String shareToken, String delYn);
}
