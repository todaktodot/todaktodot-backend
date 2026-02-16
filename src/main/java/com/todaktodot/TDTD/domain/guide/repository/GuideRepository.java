package com.todaktodot.TDTD.domain.guide.repository;

import com.todaktodot.TDTD.domain.guide.repository.entity.Guide;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuideRepository extends JpaRepository<Guide,Long> {
    Optional<Guide> findTopByDelYnOrderByGuideIdDesc(String delYn);

}
