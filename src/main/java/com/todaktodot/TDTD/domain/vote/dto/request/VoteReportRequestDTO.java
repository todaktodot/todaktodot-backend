package com.todaktodot.TDTD.domain.vote.dto.request;

import com.todaktodot.TDTD.domain.vote.repository.entity.ReportReason;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoteReportRequestDTO {
    private Long voteId;
    private ReportReason reason;
}
