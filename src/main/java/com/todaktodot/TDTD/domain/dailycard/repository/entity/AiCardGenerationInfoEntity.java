package com.todaktodot.TDTD.domain.dailycard.repository.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 카드 생성 정보 엔티티
 * AI를 통해 데일리카드를 생성할 때 사용된 설정과 프롬프트를 저장
 */
@Entity
@Table(name = "AI_CARD_GENERATION_INFO")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiCardGenerationInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "INFO_ID")
    private Long infoId;

    @Column(name = "CARD_ID", nullable = false)
    private Long cardId;

    @Column(name = "PROMPT_ID", nullable = false)
    private Long promptId;

    @Column(name = "AI_MODEL", length = 50, nullable = false)
    private String aiModel;

    @Column(name = "TEMPERATURE", precision = 3, scale = 2, nullable = false)
    private BigDecimal temperature;

    @Column(name = "MODE", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private CardMode mode;

    @Column(name = "SUBJECT", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private CardSubject subject;

    @Column(name = "TYPE", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private CardType type;

    @Column(name = "SITUATION_CATEGORY", length = 50)
    private String situationCategory;

    @Column(name = "FINAL_PROMPT", columnDefinition = "TEXT", nullable = false)
    private String finalPrompt;

    @Column(name = "AI_RESPONSE", columnDefinition = "TEXT")
    private String aiResponse;

    @CreationTimestamp
    @Column(name = "REG_DT", nullable = false, updatable = false)
    private LocalDateTime regDt;

    @Column(name = "REGR_ID", nullable = false)
    private Long regrId;

    @Builder
    public AiCardGenerationInfoEntity(Long cardId, Long promptId, String aiModel, BigDecimal temperature,
                                      CardMode mode, CardSubject subject, CardType type,
                                      String situationCategory, String finalPrompt, String aiResponse,
                                      Long regrId) {
        this.cardId = cardId;
        this.promptId = promptId;
        this.aiModel = aiModel;
        this.temperature = temperature;
        this.mode = mode;
        this.subject = subject;
        this.type = type;
        this.situationCategory = situationCategory;
        this.finalPrompt = finalPrompt;
        this.aiResponse = aiResponse;
        this.regrId = regrId;
    }
}
