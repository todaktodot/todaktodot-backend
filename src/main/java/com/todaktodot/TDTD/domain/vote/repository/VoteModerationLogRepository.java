package com.todaktodot.TDTD.domain.vote.repository;

import com.todaktodot.TDTD.domain.vote.repository.entity.VoteModerationLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VoteModerationLogRepository extends JpaRepository<VoteModerationLogEntity, Long> {

    List<VoteModerationLogEntity> findAllByVoteIdAndDelYnOrderByRegDtDesc(Long voteId, String delYn);

    @Query("""
            SELECT COUNT(l) FROM VoteModerationLogEntity l
            JOIN VoteEntity v ON v.voteId = l.voteId
            WHERE v.userId = :authorUserId AND l.newStatus = 'DELETED' AND l.delYn = 'N'
            """)
    long countDeleteConfirmedByAuthor(@Param("authorUserId") Long authorUserId);
}
