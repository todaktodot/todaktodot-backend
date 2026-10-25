package com.todaktodot.TDTD.domain.vote.dto.response;

import com.todaktodot.TDTD.domain.vote.repository.entity.VoteEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class VoteCandidateDTO {

    private final Long voteId;
    private final String category;
    private final String categoryLabel;
    private final String title;
    private final List<String> options;
    private final LocalDateTime regDt;

    public static VoteCandidateDTO of(VoteEntity vote, List<String> options) {
        return VoteCandidateDTO.builder()
                .voteId(vote.getVoteId())
                .category(vote.getCategory().name())
                .categoryLabel(vote.getCategory().getDescription())
                .title(vote.getTitle())
                .options(options)
                .regDt(vote.getRegDt())
                .build();
    }
}
