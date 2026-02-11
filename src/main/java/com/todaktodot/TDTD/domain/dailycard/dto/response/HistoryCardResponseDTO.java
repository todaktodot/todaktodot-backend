package com.todaktodot.TDTD.domain.dailycard.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class HistoryCardResponseDTO {

    private LocalDate startDate;
    private LocalDate endDate;
    private List<HistoryCardItem> historyCards;

    @Getter
    @Builder
    public static class HistoryCardItem {
        private LocalDate issuedDate;
        private String mode;
        private String subject;
        private boolean selected;
        // 선택 완료 시에만 값이 채워짐 (미선택이면 null)
        private Long coupleCardId;
        private Long cardId;
        private String type;
        private String situation;
        private Boolean user1Answered;
        private Boolean user2Answered;
    }
}
