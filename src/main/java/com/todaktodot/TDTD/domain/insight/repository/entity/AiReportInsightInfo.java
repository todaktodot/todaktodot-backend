package com.todaktodot.TDTD.domain.insight.repository.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "AI_REPORT_INSIGHT_INFO")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiReportInsightInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "INFO_ID")
    private Long infoId;

    @Column(name = "INSIGHT_ID", nullable = false)
    private Long insightId;

    @Column(name = "PROMPT_ID")
    private Long promptId;

    @Column(name = "AI_MODEL", length = 50, nullable = false)
    private String aiModel;

    @Column(name = "TEMPERATURE", nullable = false, length = 10)
    private String temperature;

    @Column(name = "FINAL_PROMPT", columnDefinition = "TEXT", nullable = false)
    private String finalPrompt;

    @Column(name = "STATUS", length = 20, nullable = false)
    private String status;

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

    @Column(name = "DEL_YN", length = 1, nullable = false)
    private String delYn = "N";

    @Builder
    public AiReportInsightInfo(Long insightId,
                               Long promptId,
                               String aiModel,
                               String temperature,
                               String finalPrompt,
                               String status,
                               Long regrId,
                               Long updrId) {
        this.insightId = insightId;
        this.promptId = promptId;
        this.aiModel = aiModel;
        this.temperature = temperature;
        this.finalPrompt = finalPrompt;
        this.status = status;
        this.regrId = regrId;
        this.updrId = updrId;
        this.delYn = "N";
    }
}
