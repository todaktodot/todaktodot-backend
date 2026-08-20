package com.todaktodot.TDTD.domain.vote_kyu.repository;

import com.todaktodot.TDTD.domain.vote.repository.entity.VoteOptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoteOptionRepository extends JpaRepository<VoteOptionEntity, Long> {
    List<VoteOptionEntity> findAllByVoteIdAndDelYn(Long voteId, String delYn);
}
