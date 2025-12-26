package com.todaktodot.TDTD.domain.dailycard.repository.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "DAILY_CARD_OPTION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(DailyCardOptionId.class)
public class DailyCardOptionEntity {

    @Id
    @Column(name = "CARD_ID")
    private Long cardId;

    @Id
    @Column(name = "QUESTION_NO")
    private Integer questionNo;

    @Id
    @Column(name = "OPTION_NO")
    private Integer optionNo;

    @Column(name = "OPTION_CNTS", length = 500, nullable = false)
    private String optionCnts;

    @CreationTimestamp
    @Column(name = "REG_DT", nullable = false, updatable = false)
    private LocalDateTime regDt;

    @Column(name = "REGR_ID", nullable = false)
    private Long regrId;

    @UpdateTimestamp
    @Column(name = "UPD_DT", nullable = false)
    private LocalDateTime updDt;

    @Column(name = "UPDR_ID", nullable = false)
    private Long updrId;

    @Column(name = "DEL_YN", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String delYn = "N";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "CARD_ID", referencedColumnName = "CARD_ID", insertable = false, updatable = false),
            @JoinColumn(name = "QUESTION_NO", referencedColumnName = "QUESTION_NO", insertable = false, updatable = false)
    })
    @Setter
    private DailyCardQuestionEntity question;

    @Builder
    public DailyCardOptionEntity(Long cardId, Integer questionNo, Integer optionNo,
                                 String optionCnts, Long regrId, Long updrId) {
        this.cardId = cardId;
        this.questionNo = questionNo;
        this.optionNo = optionNo;
        this.optionCnts = optionCnts;
        this.regrId = regrId;
        this.updrId = updrId;
        this.delYn = "N";
    }
}
