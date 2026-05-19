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
@Table(name = "DAILY_CARD_ANSWER_REACTION",
    uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_answer_reactor_active",
                    columnNames = {"ANSWER_ID", "REACTOR_USER_ID", "ACTIVE_SLOT"}
            )
    },
    indexes = {
            @Index(name = "idx_answer_active", columnList = "ANSWER_ID, DEL_YN"),
            @Index(name = "idx_reactor_active", columnList = "REACTOR_USER_ID, DEL_YN"),
            @Index(name = "idx_emoji_type", columnList = "EMOJI_TYPE, DEL_YN")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyCardAnswerReactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REACTION_ID")
    private Long reactionId;

    @Column(name = "ANSWER_ID", nullable = false)
    private Long answerId;

    @Column(name = "REACTOR_USER_ID", nullable = false)
    private Long reactorUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "EMOJI_TYPE", nullable = false, length = 20)
    private EmojiType emojiType;

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

    @Column(name = "ACTIVE_SLOT", insertable = false, updatable = false)
    private Integer activeSlot;

    @Builder
    public DailyCardAnswerReactionEntity(Long answerId, Long reactorUserId, EmojiType emojiType,
                                         Long regrId) {
        this.answerId = answerId;
        this.reactorUserId = reactorUserId;
        this.emojiType = emojiType;
        this.regrId = regrId;
        this.updrId = regrId;
        this.delYn = "N";
    }

    // 이모지 변경
    public void updateEmojiType(EmojiType emojiType, Long updrId) {
        this.emojiType = emojiType;
        this.updrId = updrId;
    }

    // 이모지 삭제
    public void updateDelYn(Long updrId) {
        this.delYn = "Y";
        this.updrId = updrId;
    }
}
