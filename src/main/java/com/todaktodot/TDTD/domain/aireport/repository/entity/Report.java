package com.todaktodot.TDTD.domain.aireport.repository.entity;

import com.todaktodot.TDTD.domain.couple.repository.entity.CoupleEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    @Column(name = "TOTAL_SYNC_RATE", nullable = false)
    private String economySyncRate;

    @Column(name = "LIFE_SYNC_RATE", nullable = false)
    private String lifeSyncRate;

    @Column(name = "LOVE_SYNC_RATE", nullable = false)
    private String loveSyncRate;

    @Column(name = "ANSWER_RATE", nullable = false)
    private String answerRate;

    @Column(name = "TOTAL_ANSWER_CNT", nullable = false)
    private String totalAnswerCnt;

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
}
