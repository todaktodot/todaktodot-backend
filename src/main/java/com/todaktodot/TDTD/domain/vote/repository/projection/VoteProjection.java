package com.todaktodot.TDTD.domain.vote.repository.projection;

import com.todaktodot.TDTD.domain.vote.repository.entity.VoteCategory;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteDisplayStatus;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface VoteProjection {

    Long getVoteId();
    String getNickname();
    VoteCategory getCategory();
    VoteStatus getStatus();
    VoteDisplayStatus getDisplayStatus();
    String getTitle();
    Integer getLikeCnt();
    Integer getParticipantCnt();
    Integer getReportCnt();
    LocalDateTime getClosedAt();
    LocalDateTime getCreatedAt();
    String getIsMine();
    String getHasVoted();
    String getHasLiked();

    // Option
    Long getOptionId();
    String getContent();
    Integer getSortOrder();
    Integer getVoteCnt();
    BigDecimal getVoteRate();
    String getIsSelected();
}
