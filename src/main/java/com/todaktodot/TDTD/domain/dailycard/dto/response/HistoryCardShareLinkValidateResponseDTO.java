package com.todaktodot.TDTD.domain.dailycard.dto.response;

import com.todaktodot.TDTD.domain.dailycard.repository.entity.ShareLinkStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class HistoryCardShareLinkValidateResponseDTO {
    private ShareLinkStatus status;
    private Long coupleCardId;
    private String message;
}
