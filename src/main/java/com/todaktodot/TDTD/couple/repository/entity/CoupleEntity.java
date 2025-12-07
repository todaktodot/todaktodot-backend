package com.todaktodot.TDTD.couple.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    @Column(name = "USER_ID_1", nullable = false, length = 50)
    private String userId1;  // 코드 발급자

    @Column(name = "USER_ID_2", nullable = false, length = 50)
    private String userId2;  // 코드 입력자

    @Column(name = "CONNECTED_DT", nullable = false)
    private LocalDateTime connectedDt;  // 커플 연결 일자

    @Column(name = "REG_DT", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime regDt;

    @Column(name = "REGR_ID", nullable = false, length = 50)
    private String regrId;

    @Column(name = "UPD_DT", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updDt;

    @Column(name = "UPDR_ID", nullable = false, length = 50)
    private String updrId;

    @Column(name = "DEL_YN", nullable = false, length = 1)
    @Builder.Default
    private String delYn = "N";
}