package com.todaktodot.TDTD.domain.vote.repository;

import com.todaktodot.TDTD.domain.vote.repository.entity.VoteSelectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoteSelectRepository extends JpaRepository<VoteSelectEntity, Long> {

    Optional<VoteSelectEntity> findByVoteIdAndUserIdAndDelYn(Long voteId, Long userId, String delYn);

    boolean existsByVoteIdAndDelYn(Long voteId, String delYn);
}
