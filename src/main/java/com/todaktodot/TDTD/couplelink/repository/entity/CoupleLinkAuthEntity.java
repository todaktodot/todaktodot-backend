package com.todaktodot.TDTD.couplelink.repository.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "COUPLE_LINK_AUTH")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CoupleLinkAuthEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COUPLE_LINK_AUTH_SEQ")
    private Long coupleLinkAuthSeq;

    @Column(name = "LINK_CODE", length = 50)
    private String linkCode;

    @Column(name = "ISSUED_USER_ID", length = 50)
    private String issuedUserId;

    @Column(name = "LINKED_USER_ID", length = 50)
    private String linkedUserId;

    @Column(name = "STATUS", length = 20)
    @Enumerated(EnumType.STRING)
    private LinkStatus status;

    @Column(name = "EXPIRED_DT")
    private LocalDateTime expiredDt;

    @Column(name = "REGR_ID", length = 50, nullable = false)
    private String regrId;

    @Column(name = "UPDR_ID", length = 50, nullable = false)
    private String updrId;

    @CreationTimestamp
    @Column(name = "REG_DT", nullable = false, updatable = false)
    private LocalDateTime regDt;

    @UpdateTimestamp
    @Column(name = "UPD_DT", nullable = false)
    private LocalDateTime updDt;

    @Column(name = "DEL_YN", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String delYn = "N";

    @Builder
    public CoupleLinkAuthEntity(String linkCode, String issuedUserId,
                                LinkStatus status, LocalDateTime expiredDt,
                                String regrId, String updrId) {
        this.linkCode = linkCode;
        this.issuedUserId = issuedUserId;
        this.status = status;
        this.expiredDt = expiredDt;
        this.regrId = regrId;
        this.updrId = updrId;
        this.delYn = "N";
    }

    public void updateStatusToExpired(String updrId) {
        this.status = LinkStatus.EXPIRED;
        this.updrId = updrId;
    }

    public void linkCouple(String linkedUserId, String updrId) {
        this.linkedUserId = linkedUserId;
        this.status = LinkStatus.LINKED;
        this.updrId = updrId;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiredDt);
    }

    public boolean isIssued() {
        return this.status == LinkStatus.ISSUED;
    }
}