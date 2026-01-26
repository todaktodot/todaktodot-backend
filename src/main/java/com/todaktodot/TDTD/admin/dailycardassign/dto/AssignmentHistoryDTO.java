package com.todaktodot.TDTD.admin.dailycardassign.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AssignmentHistoryDTO {

    private Long coupleCardId;
    private Long coupleId;
    private String user1Name;
    private String user2Name;
    private LocalDate issuedDate;
    private Long cardId;
    private String cardTitle;
    private String cardMode;
    private String cardModeDisplayName;
    private String cardSubject;
    private String cardSubjectDisplayName;
    private String cardType;
    private String cardTypeDisplayName;
    private LocalDateTime regDt;

    public String getCoupleName() {
        String name1 = user1Name != null ? user1Name : "?";
        String name2 = user2Name != null ? user2Name : "?";
        return name1 + " / " + name2;
    }
}
