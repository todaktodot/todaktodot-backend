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
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "DAILY_CARD_QUESTION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(DailyCardQuestionId.class)
public class DailyCardQuestionEntity {

    @Id
    @Column(name = "CARD_ID")
    private Long cardId;

    @Id
    @Column(name = "QUESTION_NO")
    private Integer questionNo;

    @Column(name = "QUESTION_TYPE", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestionType questionType;

    @Column(name = "ANSWER_REQ_YN", length = 1, columnDefinition = "CHAR(1) DEFAULT 'Y'")
    private String answerReqYn = "Y";

    @Column(name = "QUESTION_CNTS", length = 1000, nullable = false)
    private String questionCnts;

    @CreationTimestamp
    @Column(name = "REG_DT", nullable = false, updatable = false)
    private LocalDateTime regDt;

    @Column(name = "REGR_ID", nullable = false, length = 50)
    private String regrId;

    @UpdateTimestamp
    @Column(name = "UPD_DT", nullable = false)
    private LocalDateTime updDt;

    @Column(name = "UPDR_ID", nullable = false, length = 50)
    private String updrId;

    @Column(name = "DEL_YN", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String delYn = "N";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_ID", insertable = false, updatable = false)
    @Setter
    private DailyCardEntity dailyCard;

    @OneToMany(mappedBy = "question")
    @OrderBy("optionNo ASC")
    private Set<DailyCardOptionEntity> options = new HashSet<>();

    @Builder
    public DailyCardQuestionEntity(Long cardId, Integer questionNo, QuestionType questionType,
                                   String answerReqYn, String questionCnts,
                                   String regrId, String updrId) {
        this.cardId = cardId;
        this.questionNo = questionNo;
        this.questionType = questionType;
        this.answerReqYn = answerReqYn;
        this.questionCnts = questionCnts;
        this.regrId = regrId;
        this.updrId = updrId;
        this.delYn = "N";
    }

    public void addOption(DailyCardOptionEntity option) {
        this.options.add(option);
        option.setQuestion(this);
    }
}
