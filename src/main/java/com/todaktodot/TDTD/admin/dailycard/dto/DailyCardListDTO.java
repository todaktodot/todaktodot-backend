package com.todaktodot.TDTD.admin.dailycard.dto;

import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardMode;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardSubject;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardType;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.DailyCardEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class DailyCardListDTO {

    private Long cardId;
    private CardMode mode;
    private CardSubject subject;
    private CardType type;
    private String cardTitle;
    private Long questionCount;
    private String useYn;
    private LocalDateTime regDt;
    private Long regrId;
    private String regrNm;
    private LocalDateTime updDt;
    private Long updrId;
    private String updrNm;
    private int optionCount;

    /**
     * QueryDSL Projections.constructor 용 생성자
     */
    public DailyCardListDTO(Long cardId, CardMode mode, CardSubject subject, CardType type,
                            String cardTitle, Long questionCount, String useYn,
                            LocalDateTime regDt, Long regrId, String regrNm,
                            LocalDateTime updDt, Long updrId, String updrNm,
                            Integer optionCount) {
        this.cardId = cardId;
        this.mode = mode;
        this.subject = subject;
        this.type = type;
        this.cardTitle = cardTitle;
        this.questionCount = questionCount != null ? questionCount : 0L;
        this.useYn = useYn;
        this.regDt = regDt;
        this.regrId = regrId;
        this.regrNm = regrNm;
        this.updDt = updDt;
        this.updrId = updrId;
        this.updrNm = updrNm;
        this.optionCount = optionCount != null ? optionCount : 0;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static DailyCardListDTO fromEntity(DailyCardEntity entity) {
        return DailyCardListDTO.builder()
                .cardId(entity.getCardId())
                .mode(entity.getMode())
                .subject(entity.getSubject())
                .type(entity.getType())
                .cardTitle(entity.getCardTitle())
                .questionCount((long) entity.getQuestions().size())
                .useYn(entity.getUseYn())
                .regDt(entity.getRegDt())
                .regrId(entity.getRegrId())
                .updDt(entity.getUpdDt())
                .updrId(entity.getUpdrId())
                .optionCount(0)
                .build();
    }

    public String getModeDisplayName() {
        return mode != null ? mode.getDisplayName() : "";
    }

    public String getSubjectDisplayName() {
        return subject != null ? subject.getDisplayName() : "";
    }

    public String getTypeDisplayName() {
        return type != null ? type.getDisplayName() : "";
    }

    public boolean isActive() {
        return "Y".equals(useYn);
    }
}
