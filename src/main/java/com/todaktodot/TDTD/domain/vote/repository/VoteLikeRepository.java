package com.todaktodot.TDTD.domain.vote.repository;

import com.todaktodot.TDTD.domain.vote.repository.entity.VoteLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoteLikeRepository extends JpaRepository<VoteLikeEntity, Long> {

    Optional<VoteLikeEntity> findByVoteIdAndUserIdAndDelYn(Long voteId, Long userId, String delYn);

    long countByVoteIdAndDelYn(Long voteId, String delYn);
}
