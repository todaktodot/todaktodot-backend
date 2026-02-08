package com.todaktodot.TDTD.domain.insight.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "INSIGHT")
public class Insight {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "COUPLE_ID", nullable = false)
    private Long coupleId;

    @Column(name = "START_DT", nullable = false)
    private LocalDate startDt;

    @Column(name = "END_DT", nullable = false)
    private LocalDate endDt;

    @Column(name = "SUMMARY", nullable = false)
    private String summary;

    @Column(name = "ECONOMY_PART")
    private String economyPart;

    @Column(name = "LIFESTYLE_PART")
    private String lifestylePart;

    @Column(name = "LOVE_PART")
    private String lovePart;

    @Column(name = "REG_DT", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime regDt;

    @Column(name = "REGR_ID", nullable = false, columnDefinition = "BIGINT")
    private Long regrId;

    @Column(name = "UPD_DT", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updDt;

    @Column(name = "UPDR_ID", nullable = false, columnDefinition = "BIGINT")
    private Long updrId;

    @Column(name = "DEL_YN", nullable = false, length = 1)
    @Builder.Default
    private String delYn = "N";

    public void softDelete(Long userId) {
        this.delYn = "Y";
        this.updrId = userId;
    }
}
