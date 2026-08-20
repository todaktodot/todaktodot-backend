package com.todaktodot.TDTD.domain.vote.repository;

import com.todaktodot.TDTD.domain.vote.repository.entity.VoteOptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoteOptionRepository extends JpaRepository<VoteOptionEntity, Long> {

    List<VoteOptionEntity> findByVoteIdAndDelYnOrderBySortOrderAsc(Long voteId, String delYn);

    boolean existsByOptionIdAndVoteIdAndDelYn(Long optionId, Long voteId, String delYn);

    List<VoteOptionEntity> findAllByVoteIdAndDelYn(Long voteId, String delYn);
}
