package com.todaktodot.TDTD.domain.vote.repository;

import com.todaktodot.TDTD.domain.vote.repository.entity.VoteSelectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoteSelectRepository extends JpaRepository<VoteSelectEntity, Long> {

    boolean existsByVoteIdAndDelYn(Long voteId, String delYn);

    Optional<VoteSelectEntity> findByVoteIdAndUserIdAndDelYn(Long voteId, Long userId, String delYn);

    long countByOptionIdAndDelYn(Long optionId, String delYn);
}
