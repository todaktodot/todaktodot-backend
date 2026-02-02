package com.todaktodot.TDTD.domain.feedback.repository.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "AI_FEEDBACK_CONFIG")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiFeedbackConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CONFIG_ID")
    private Long configId;

    @Column(name = "PROMPT_ID")
    private Long promptId;

    @Column(name = "AI_MODEL", length = 50, nullable = false, columnDefinition = "VARCHAR(50) DEFAULT 'gpt-4o-mini'")
    private String aiModel = "gpt-4o-mini";

    @Column(name = "TEMPERATURE", nullable = false, columnDefinition = "DECIMAL(3,2) DEFAULT 0.70")
    private BigDecimal temperature = BigDecimal.valueOf(0.70);

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

    @Column(name = "DEL_YN", length = 1, nullable = false, columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String delYn = "N";

    @Builder
    public AiFeedbackConfigEntity(Long promptId, String aiModel, BigDecimal temperature,
                                  Long regrId, Long updrId) {
        this.promptId = promptId;
        this.aiModel = aiModel != null ? aiModel : "gpt-4o-mini";
        this.temperature = temperature != null ? temperature : BigDecimal.valueOf(0.70);
        this.regrId = regrId;
        this.updrId = updrId;
        this.delYn = "N";
    }

    public void softDelete(Long updrId) {
        this.delYn = "Y";
        this.updrId = updrId;
    }
}
