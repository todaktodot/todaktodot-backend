package com.todaktodot.TDTD.domain.vote.repository;

import com.todaktodot.TDTD.domain.vote.repository.entity.VoteEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VoteRepository extends JpaRepository<VoteEntity, Long> {

    Optional<VoteEntity> findByVoteIdAndDelYn(Long voteId, String delYn);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM VoteEntity v WHERE v.voteId = :voteId AND v.delYn = 'N'")
    Optional<VoteEntity> findByVoteIdForUpdate(@Param("voteId") Long voteId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE VoteEntity v
            SET v.participantCnt = v.participantCnt + 1, v.updrId = :userId
            WHERE v.voteId = :voteId
            """)
    void increaseParticipantCnt(@Param("voteId") Long voteId, @Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE VoteEntity v
            SET v.participantCnt = v.participantCnt - 1, v.updrId = :userId
            WHERE v.voteId = :voteId AND v.participantCnt > 0
            """)
    void decreaseParticipantCnt(@Param("voteId") Long voteId, @Param("userId") Long userId);
}
