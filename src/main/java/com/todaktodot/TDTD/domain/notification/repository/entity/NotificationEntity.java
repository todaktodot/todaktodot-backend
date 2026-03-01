package com.todaktodot.TDTD.domain.notification.repository.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "NOTIFICATION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NOTIFICATION_ID")
    private Long notificationId;

    @Column(name = "FCM_TOKEN", nullable = false, length = 512)
    private String fcmToken;

    @Column(name = "PUSH_TYPE", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PushType pushType;

    @Column(name = "COUPLE_DAILY_CARD_ID", nullable = false)
    private Long coupleDailyCardId;

    //받는사람
    @Column(name = "RECEIVE_USER", nullable = false)
    private Long receiveUser;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "CONTENT")
    private String conent;

    @Column(name = "SUCCESS_YN",length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String successYn;

    @CreationTimestamp
    @Column(name = "REG_DT", nullable = false, updatable = false)
    private LocalDateTime regDt;

    @Column(name = "DEL_YN", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String delYn = "N";

    @Builder
    public NotificationEntity(String fcmToken,PushType pushType, Long coupleDailyCardId, Long receiveUser, String title, String conent,String successYn) {
        this.fcmToken = fcmToken;
        this.pushType = pushType;
        this.coupleDailyCardId = coupleDailyCardId;
        this.receiveUser = receiveUser;
        this.title = title;
        this.conent = conent;
        this.successYn = successYn;
    }

    //soft삭제
    public void softDelete() {
        this.delYn = "Y";
    }
}
