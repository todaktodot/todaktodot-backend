package com.todaktodot.TDTD.domain.couple.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Column(name = "USER_ID_2", nullable = false, columnDefinition = "BIGINT")
    private Long userId2;  // 코드 입력자

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

    public void updateCoupleInfo(LocalDate firstMetDt, RelationshipStage relationshipStage, Long updrId) {
        this.firstMetDt = firstMetDt;
        this.relationshipStage = relationshipStage;
        this.updrId = updrId;
    }
}