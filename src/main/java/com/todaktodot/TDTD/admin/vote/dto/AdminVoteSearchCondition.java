package com.todaktodot.TDTD.admin.vote.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AdminVoteSearchCondition {

    private String category;       // LOVE, ECONOMY, LIFESTYLE
    private String voteStatus;     // ACTIVE, CLOSED, AUTO_HIDDEN, HIDDEN
    private String reportStatus;   // NONE, PENDING, RESOLVED
    private String sort = "LATEST"; // LATEST, REPORT_DESC, WAIT_ASC
    private LocalDate startDt;
    private LocalDate endDt;
    private String keyword;        // 질문, 랜덤닉네임, user_id 통합 검색
    private int page = 0;
    private int size = 10;

    public boolean hasCategory() {
        return category != null && !category.isBlank();
    }

    public boolean hasVoteStatus() {
        return voteStatus != null && !voteStatus.isBlank();
    }

    public boolean hasReportStatus() {
        return reportStatus != null && !reportStatus.isBlank();
    }

    public boolean hasKeyword() {
        return keyword != null && !keyword.isBlank();
    }
}
