package com.todaktodot.TDTD.domain.feedback.repository;

import com.todaktodot.TDTD.domain.feedback.repository.entity.AiFeedbackConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiFeedbackConfigRepository extends JpaRepository<AiFeedbackConfigEntity, Long> {

    Optional<AiFeedbackConfigEntity> findTopByDelYnOrderByConfigIdDesc(String delYn);
}
