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
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "DAILY_CARD_SHARE_LINK")
public class DailyCardShareLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shareLinkId;

    @Column(nullable = false, unique = true, length = 100)
    private String shareToken;

    @Column(nullable = false)
    private Long coupleCardId;

    @Column(nullable = false)
    private Long createUserId;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    @CreationTimestamp
    @Column(name = "REG_DT", nullable = false, updatable = false)
    private LocalDateTime regDt;

    @Column(name = "REGR_ID", nullable = false)
    private Long regrId;

    @UpdateTimestamp
    @Column(name = "UPD_DT")
    private LocalDateTime updDt;

    @Column(name = "UPDR_ID")
    private Long updrId;

    @Column(nullable = false, length = 1)
    private String delYn;

    @Builder
    public DailyCardShareLink(
            String shareToken,
            Long coupleCardId,
            Long createUserId,
            LocalDateTime expiredAt
    ) {
        this.shareToken = shareToken;
        this.coupleCardId = coupleCardId;
        this.createUserId = createUserId;
        this.expiredAt = expiredAt;
        this.delYn = "N";
        this.regrId = createUserId;
    }

    public boolean isExpired(LocalDateTime now) {
        return expiredAt.isBefore(now);
    }
}
