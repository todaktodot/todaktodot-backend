package com.todaktodot.TDTD.domain.vote.repository.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "VOTE_CANDIDATE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoteCandidateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CANDIDATE_ID")
    private Long candidateId;

    // 한 번의 AI 호출로 나온 후보들을 묶는 키. yyyy-MM-dd 형식.
    @Column(name = "BATCH_KEY", nullable = false, length = 20)
    private String batchKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "CATEGORY", nullable = false, length = 20)
    private VoteCategory category;

    // VOTE.TITLE 과 같은 길이 제한. 어드민이 등록 전에 수정할 수 있다.
    @Column(name = "TITLE", nullable = false, length = 100)
    private String title;

    // 초안이라 자식 테이블 없이 JSON 배열 문자열로 보관한다. 각 항목은 VOTE_OPTION.CONTENT 제한과 같은 20자.
    @Column(name = "OPTIONS_JSON", nullable = false, length = 500)
    private String optionsJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private VoteCandidateStatus status = VoteCandidateStatus.PENDING;

    // STATUS 가 APPROVED 일 때만 값이 존재한다.
    @Column(name = "VOTE_ID")
    private Long voteId;

    @Column(name = "PROMPT_ID")
    private Long promptId;

    @Column(name = "AI_MODEL", length = 50)
    private String aiModel;

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

    @Column(name = "DEL_YN", nullable = false, length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String delYn = "N";

    @Builder
    public VoteCandidateEntity(String batchKey, VoteCategory category, String title, String optionsJson,
                               Long promptId, String aiModel, Long regrId) {
        this.batchKey = batchKey;
        this.category = category;
        this.title = title;
        this.optionsJson = optionsJson;
        this.promptId = promptId;
        this.aiModel = aiModel;
        this.status = VoteCandidateStatus.PENDING;
        this.regrId = regrId;
        this.updrId = regrId;
        this.delYn = "N";
    }

    // 등록 완료 - 생성된 투표 ID 를 남겨 중복 등록을 막는다.
    public void approve(Long voteId, Long updrId) {
        this.status = VoteCandidateStatus.APPROVED;
        this.voteId = voteId;
        this.updrId = updrId;
        this.updDt = LocalDateTime.now();
    }

    public void reject(Long updrId) {
        this.status = VoteCandidateStatus.REJECTED;
        this.updrId = updrId;
        this.updDt = LocalDateTime.now();
    }

    // 어드민이 등록 직전에 고친 문구를 반영
    public void updateContent(String title, String optionsJson, Long updrId) {
        this.title = title;
        this.optionsJson = optionsJson;
        this.updrId = updrId;
        this.updDt = LocalDateTime.now();
    }
}
