package com.todaktodot.TDTD.domain.dailycard.dto.response;

import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardType;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CoupleDailyCardEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class SelectCardTypeResponseDTO {

    private Long coupleCardId;
    private Long cardId;
    private LocalDate issuedDate;
    private CardType selectedType;
    private LocalDateTime selectedAt;

    public static SelectCardTypeResponseDTO from(CoupleDailyCardEntity entity) {
        return SelectCardTypeResponseDTO.builder()
                .coupleCardId(entity.getCoupleCardId())
                .cardId(entity.getCardId())
                .issuedDate(entity.getIssuedDate())
                .selectedType(entity.getSelectedType())
                .selectedAt(entity.getUpdDt())
                .build();
    }
}
