package com.todaktodot.TDTD.domain.dailycard.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class HistoryCardShareLinkResponseDTO {
    private String shareUrl;
    private String shareToken;
    private LocalDateTime expiredAt;
}
