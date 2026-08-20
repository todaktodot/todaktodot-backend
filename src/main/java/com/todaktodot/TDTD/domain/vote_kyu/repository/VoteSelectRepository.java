package com.todaktodot.TDTD.domain.vote_kyu.repository;

import com.todaktodot.TDTD.domain.vote.repository.entity.VoteOptionEntity;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteSelectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteSelectRepository extends JpaRepository<VoteSelectEntity, Long> {
    boolean existsByVoteIdAndDelYn(Long voteId, String delYn);
}
