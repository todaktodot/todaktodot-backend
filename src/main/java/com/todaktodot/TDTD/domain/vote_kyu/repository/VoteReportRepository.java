package com.todaktodot.TDTD.domain.vote_kyu.repository;

import com.todaktodot.TDTD.domain.vote.repository.entity.VoteReportEntity;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteSelectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VoteReportRepository extends JpaRepository<VoteReportEntity, Long> {
    Optional<VoteReportEntity> findByVoteIdAndUserId(Long voteId, Long userId);

    List<VoteReportEntity> findAllByVoteId(Long voteId);
}
