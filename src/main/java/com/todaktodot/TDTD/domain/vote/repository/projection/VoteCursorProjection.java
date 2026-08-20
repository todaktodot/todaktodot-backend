package com.todaktodot.TDTD.domain.vote.repository.projection;

import java.time.LocalDateTime;

public interface VoteCursorProjection {

    Long getVoteId();
    LocalDateTime getCreatedAt();
    Integer getParticipantCnt();
}
