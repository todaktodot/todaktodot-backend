package com.todaktodot.TDTD.domain.vote.repository;

import com.todaktodot.TDTD.domain.vote.repository.entity.VoteCandidateEntity;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteCandidateStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VoteCandidateRepository extends JpaRepository<VoteCandidateEntity, Long> {

    List<VoteCandidateEntity> findAllByStatusAndDelYnOrderByRegDtDescCandidateIdDesc(VoteCandidateStatus status, String delYn);

    Optional<VoteCandidateEntity> findByCandidateIdAndDelYn(Long candidateId, String delYn);
}
