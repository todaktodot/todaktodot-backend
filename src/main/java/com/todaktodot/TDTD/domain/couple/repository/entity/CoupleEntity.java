package com.todaktodot.TDTD.domain.couple.repository.entity;

import com.todaktodot.TDTD.domain.aireport.repository.entity.Report;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
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
@Table(name = "COUPLE")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoupleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COUPLE_ID")
    private Long coupleId;

    @Column(name = "USER_ID_1", nullable = false, columnDefinition = "BIGINT")
    private Long userId1;  // 코드 발급자

    @Column(name = "USER_ID_2", nullable = true, columnDefinition = "BIGINT")
    private Long userId2;  // 코드 입력자 (NULL = 혼자 둘러보기)

    @Column(name = "COUPLE_TYPE", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CoupleType coupleType = CoupleType.CONNECTED;  // 커플 유형

    @Column(name = "CONNECTED_DT", nullable = false)
    private LocalDateTime connectedDt;  // 커플 연결 일자

    @Column(name = "FIRST_MET_DT")
    private LocalDate firstMetDt;  // 우리가 만난 날

    @Column(name = "RELATIONSHIP_STAGE", length = 30)
    @Enumerated(EnumType.STRING)
    private RelationshipStage relationshipStage;  // 관계 단계

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

    @OneToMany(mappedBy = "coupleEntity", cascade = CascadeType.PERSIST)
    private List<Report> reportList = new ArrayList<>();

    public void updateCoupleInfo(LocalDate firstMetDt, RelationshipStage relationshipStage, Long updrId) {
        this.firstMetDt = firstMetDt;
        this.relationshipStage = relationshipStage;
        this.updrId = updrId;
    }

    public void disconnect(Long updrId) {
        this.delYn = "Y";
        this.updrId = updrId;
    }

    /**
     * 커플 연결이 완료되었는지 확인 (CONNECTED 상태이고 userId2가 존재)
     */
    public boolean isComplete() {
        return this.userId2 != null && this.coupleType == CoupleType.CONNECTED;
    }

    /**
     * 혼자 둘러보기 상태인지 확인
     */
    public boolean isSolo() {
        return this.coupleType == CoupleType.SOLO;
    }

    /**
     * 상대방 연결 (SOLO → CONNECTED 전환)
     */
    public void connectPartner(Long partnerId, Long updrId) {
        this.userId2 = partnerId;
        this.coupleType = CoupleType.CONNECTED;
        this.connectedDt = LocalDateTime.now();
        this.updrId = updrId;
    }

    /**
     * 소프트 삭제 (disconnect와 동일, 명시적 이름)
     */
    public void softDelete(Long updrId) {
        this.delYn = "Y";
        this.updrId = updrId;
    }
}