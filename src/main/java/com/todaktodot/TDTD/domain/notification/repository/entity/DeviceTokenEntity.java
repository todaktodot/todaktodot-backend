package com.todaktodot.TDTD.domain.notification.repository.entity;

import com.todaktodot.TDTD.domain.login.respository.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "DEVICE_TOKEN")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DEVICE_TOKEN_SEQ")
    private Long deviceTokenSeq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "FCM_TOKEN", nullable = false, length = 512)
    private String fcmToken;

    @Column(name = "DEVICE_TYPE", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    @Column(name = "OS_VERSION", length = 50)
    private String osVersion;  // 예: "iOS 17.2", "Android 14"

    @Column(name = "APP_VERSION", length = 20)
    private String appVersion;  // 예: "1.0.0", "1.2.3"

    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive;

    @Column(name = "REGR_ID", nullable = false, columnDefinition = "BIGINT")
    private Long regrId;

    @Column(name = "UPDR_ID", nullable = false, columnDefinition = "BIGINT")
    private Long updrId;

    @CreationTimestamp
    @Column(name = "REG_DT", nullable = false, updatable = false)
    private LocalDateTime regDt;

    @UpdateTimestamp
    @Column(name = "UPD_DT", nullable = false)
    private LocalDateTime updDt;

    @Column(name = "DEL_YN", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String delYn = "N";

    @Builder
    public DeviceTokenEntity(User user, String fcmToken, DeviceType deviceType,
                             String osVersion, String appVersion,
                             Long regrId, Long updrId) {
        this.user = user;
        this.fcmToken = fcmToken;
        this.deviceType = deviceType;
        this.osVersion = osVersion;
        this.appVersion = appVersion;
        this.isActive = true;
        this.regrId = regrId;
        this.updrId = updrId;
        this.delYn = "N";
    }

    public void updateToken(String fcmToken, String osVersion, String appVersion, Long updrId) {
        this.fcmToken = fcmToken;
        this.osVersion = osVersion;
        this.appVersion = appVersion;
        this.isActive = true;
        this.updrId = updrId;
    }

    public void deactivate(Long updrId) {
        this.isActive = false;
        this.updrId = updrId;
    }

    public void activate(Long updrId) {
        this.isActive = true;
        this.updrId = updrId;
    }
}
