package com.todaktodot.TDTD.domain.vote.repository;

import com.todaktodot.TDTD.domain.vote.repository.entity.VoteCandidateEntity;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteCandidateStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface VoteCandidateRepository extends JpaRepository<VoteCandidateEntity, Long> {

    List<VoteCandidateEntity> findAllByStatusAndDelYnOrderByRegDtDescCandidateIdDesc(VoteCandidateStatus status, String delYn);

    //승인·반려가 동시에 들어오면 둘 다 PENDING 을 보고 투표가 두 번 게시된다
    //어드민은 공유 계정 하나라 겹칠 여지가 크다. 먼저 온 쪽이 커밋할 때까지 뒤는 대기
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<VoteCandidateEntity> findByCandidateIdAndDelYn(Long candidateId, String delYn);
}
