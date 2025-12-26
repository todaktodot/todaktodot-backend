package com.todaktodot.TDTD.domain.dailycard.repository.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "DAILY_CARD_USER_ANSWER")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyCardUserAnswerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ANSWER_ID")
    private Long answerId;

    @Column(name = "COUPLE_CARD_ID", nullable = false)
    private Long coupleCardId;

    @Column(name = "CARD_ID", nullable = false)
    private Long cardId;

    @Column(name = "QUESTION_NO", nullable = false)
    private Integer questionNo;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Column(name = "ANSWER_CONTENT", nullable = false, length = 2000)
    private String answerContent;

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

    @Column(name = "DEL_YN", nullable = false, length = 1)
    private String delYn = "N";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "CARD_ID", referencedColumnName = "CARD_ID", insertable = false, updatable = false),
            @JoinColumn(name = "QUESTION_NO", referencedColumnName = "QUESTION_NO", insertable = false, updatable = false)
    })
    private DailyCardQuestionEntity question;

    @Builder
    public DailyCardUserAnswerEntity(Long coupleCardId, Long cardId, Integer questionNo,
                                      Long userId, String answerContent,
                                      Long regrId, Long updrId) {
        this.coupleCardId = coupleCardId;
        this.cardId = cardId;
        this.questionNo = questionNo;
        this.userId = userId;
        this.answerContent = answerContent;
        this.regrId = regrId;
        this.updrId = updrId;
        this.delYn = "N";
    }

    public void updateAnswer(String answerContent, Long updrId) {
        this.answerContent = answerContent;
        this.updrId = updrId;
    }
}
