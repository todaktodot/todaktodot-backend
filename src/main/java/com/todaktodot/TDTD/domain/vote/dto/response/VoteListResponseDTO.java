package com.todaktodot.TDTD.domain.vote.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class VoteListResponseDTO {
    private List<VoteResponseDTO> data;
    private Integer createVoteCnt;
    private Boolean isSuspended;
    private String nextCursor;
    private Boolean hasNext;
}
