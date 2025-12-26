package com.todaktodot.TDTD.domain.dailycard.repository.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "COUPLE_DAILY_CARD")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CoupleDailyCardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COUPLE_CARD_ID")
    private Long coupleCardId;

    @Column(name = "COUPLE_ID", nullable = false)
    private Long coupleId;

    @Column(name = "CARD_ID", nullable = false)
    private Long cardId;

    @Column(name = "ISSUED_DATE", nullable = false)
    private LocalDate issuedDate;

    @Column(name = "SELECTED_TYPE", length = 20)
    @Enumerated(EnumType.STRING)
    private CardType selectedType;

    @Column(name = "SELECTED_BY_USER_ID")
    private Long selectedByUserId;

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
    @JoinColumn(name = "CARD_ID", insertable = false, updatable = false)
    private DailyCardEntity dailyCard;

    @Builder
    public CoupleDailyCardEntity(Long coupleId, Long cardId, LocalDate issuedDate,
                                  Long regrId, Long updrId) {
        this.coupleId = coupleId;
        this.cardId = cardId;
        this.issuedDate = issuedDate;
        this.regrId = regrId;
        this.updrId = updrId;
        this.delYn = "N";
    }

    public void selectType(CardType selectedType, Long selectedByUserId) {
        this.selectedType = selectedType;
        this.selectedByUserId = selectedByUserId;
        this.updrId = selectedByUserId;
    }
}
