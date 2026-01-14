package com.todaktodot.TDTD.domain.login.respository.entity;

import com.todaktodot.TDTD.global.security.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@Table(name = "USERS")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "USER_EMAIL")
    private String email;
    @Column(name = "USER_NAME")
    private String name;

    @Column(name = "NICK_NAME", length = 20)
    private String nickname;

    @Column(name = "USER_ROLE")
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "PROVIDER")
    private String provider; // google, kakao, apple

    @Column(name = "PROVIDER_ID")
    private String providerId;

    @Column(name = "ALARM_YN")
    private String alarmYN;

    @Column(name = "JOIN_YN")
    private String joinYN;

    @Column(name = "REG_DT", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime regDt;

    @Column(name = "REGR_ID", columnDefinition = "BIGINT")
    private Long regrId;

    @Column(name = "UPD_DT", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updDt;

    @Column(name = "UPDR_ID", columnDefinition = "BIGINT")
    private Long updrId;

    @Column(name = "DEL_YN", nullable = false, length = 1)
    @Builder.Default
    private String delYn = "N";

    @Builder
    public User(String email, String name, String nickname, Role role, String provider, String providerId, String joinYN) {
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.role = role;
        this.provider = provider;
        this.providerId = providerId;
        this.joinYN = joinYN;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 푸시알림 변경
     */
    public void updateAlarmYN(String alarmYN, Long userId) {
        if (alarmYN.equals("Y")) {
            this.alarmYN = "Y";
        }
        else {
            this.alarmYN = "N";
        }
        this.updrId = userId;
    }

    /**
     * 가입여부 변경
     */
    public void updateJoinYN(String joinYN, Long userId) {
        if (joinYN.equals("Y")) {
            this.joinYN = "Y";
        }
        else {
            this.joinYN = "N";
        }
        this.updrId = userId;
    }
}
