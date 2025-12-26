package com.todaktodot.TDTD.domain.dailycard.repository.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "DAILY_CARD")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyCardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CARD_ID")
    private Long cardId;

    @Column(name = "MODE", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private CardMode mode;

    @Column(name = "SUBJECT", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private CardSubject subject;

    @Column(name = "TYPE", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private CardType type;

    @Column(name = "CARD_TITLE", length = 500, nullable = false)
    private String cardTitle;

    @Column(name = "USE_YN", length = 1, columnDefinition = "CHAR(1) DEFAULT 'Y'")
    private String useYn = "Y";

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

    @OneToMany(mappedBy = "dailyCard")
    @OrderBy("questionNo ASC")
    private Set<DailyCardQuestionEntity> questions = new HashSet<>();

    @Builder
    public DailyCardEntity(CardMode mode, CardSubject subject, CardType type,
                           String cardTitle, String regrId, String updrId) {
        this.mode = mode;
        this.subject = subject;
        this.type = type;
        this.cardTitle = cardTitle;
        this.useYn = "Y";
        this.regrId = regrId;
        this.updrId = updrId;
        this.delYn = "N";
    }

    public void addQuestion(DailyCardQuestionEntity question) {
        this.questions.add(question);
        question.setDailyCard(this);
    }
}