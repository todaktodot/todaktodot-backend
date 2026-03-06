package com.todaktodot.TDTD.domain.aireport.repository.entity;

import com.todaktodot.TDTD.domain.couple.repository.entity.CoupleEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REPORT_ID")
    private Long id;

    @Column(name = "TOTAL_SYNC_RATE", nullable = false)
    private String totalSyncRate;

    @Column(name = "ECONOMY_SYNC_RATE", nullable = false)
    private String economySyncRate;

    @Column(name = "LIFE_SYNC_RATE", nullable = false)
    private String lifeSyncRate;

    @Column(name = "LOVE_SYNC_RATE", nullable = false)
    private String loveSyncRate;

    @Column(name = "ANSWER_RATE", nullable = false)
    private String answerRate;

    @Column(name = "TOTAL_ANSWER_CNT", nullable = false)
    private String totalAnswerCnt;

    @Column(name = "STRT_DT", nullable = false)
    private LocalDate strtDt;

    @Column(name = "END_DT", nullable = false)
    private LocalDate endDt;

    @Column(name = "INSIGHT_ID")
    private Long insightId;

    @Column(name = "READ_YN", nullable = false, length = 1)
    private String readYn = "N";

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
    //@Builder.Default
    private String delYn = "N";

    @OneToMany(mappedBy = "report", cascade = CascadeType.PERSIST)
    private List<SimilarAnswer> similarAnswerList = new ArrayList<>();

    @OneToMany(mappedBy = "report", cascade = CascadeType.PERSIST)
    private List<DiffrentAnswer> differentAnswerList = new ArrayList<>();

    //연결된 커플 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COUPLE_ID")
    private CoupleEntity coupleEntity;

    @Builder
    public Report(String totalSyncRate, String economySyncRate, String lifeSyncRate, String loveSyncRate, String answerRate, String totalAnswerCnt, Long insightId, LocalDate strtDt, LocalDate endDt, Long regrId, Long updrId, String delYn, CoupleEntity coupleEntity) {
        this.totalSyncRate = totalSyncRate;
        this.economySyncRate = economySyncRate;
        this.lifeSyncRate = lifeSyncRate;
        this.loveSyncRate = loveSyncRate;
        this.answerRate = answerRate;
        this.totalAnswerCnt = totalAnswerCnt;
        this.insightId = insightId;
        this.readYn = "N";
        this.strtDt = strtDt;
        this.endDt = endDt;
        this.regrId = regrId;
        this.updrId = updrId;
        this.delYn = delYn;
        this.coupleEntity = coupleEntity;
    }

    public void addSimilarAnswer(List<SimilarAnswer> similarAnswerList) {
        this.similarAnswerList.addAll(similarAnswerList);
        similarAnswerList.forEach(sa -> sa.addReport(this));
    }
    public void addDifferentAnswer(List<DiffrentAnswer> diffrentAnswerList) {
        this.differentAnswerList.addAll(diffrentAnswerList);
        diffrentAnswerList.forEach(da -> da.addReport(this));
    }

    public void updateInsight(Long insightId) {
        this.insightId = insightId;
        this.updrId = 0L;
    }

    public void updateReadYn(Long userId) {
        this.readYn = "Y";
        this.updrId = userId;
    }
}
