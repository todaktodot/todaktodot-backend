package com.todaktodot.TDTD.domain.login.respository.entity;

import com.todaktodot.TDTD.global.security.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "NICK_NAME", length = 20)
    private String nickname;

    @Column(name = "BIRTH_DATE")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "USER_ROLE")
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "INFO_ALARM_YN", length = 1, nullable = false)
    @Builder.Default
    private String infoAlarmYN = "N";

    @Column(name = "AD_ALARM_YN", length = 1, nullable = false)
    @Builder.Default
    private String adAlarmYN = "N";

    @Column(name = "MARKETING_ALARM_YN", length = 1, nullable = false)
    @Builder.Default
    private String marketingAlarmYN = "N";

//    @Column(name = "JOIN_YN", nullable = false, length = 1)
//    @Builder.Default
//    private String joinYN = "N";

    @Column(name = "TERM_YN", nullable = false, length = 1)
    @Builder.Default
    private String termYN = "N";

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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAccount> socialAccounts = new ArrayList<>();

//    @Builder
//    public User(String nickname, Role role, String joinYN, UserAccount userAccount) {
//        this.nickname = nickname;
//        this.role = role;
//        this.joinYN = joinYN;
//        this.socialAccounts.add(userAccount);
//    }

    public void updateUserInfo(String nickname, LocalDate birthDate, Gender gender) {
        this.nickname = nickname;
        this.birthDate = birthDate;
        this.gender = gender;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 알림 정보 변경
     */
    public void updateAlarmYN(String infoAlarmYN, String adAlarmYN, String marketingAlarmYN, Long userId) {
        //정보성 알림
        if (infoAlarmYN != null) {
            this.infoAlarmYN = infoAlarmYN.equals("Y") ? "Y" : "N";
        }
        //광고성 알림
        if (adAlarmYN != null) {
            this.adAlarmYN = adAlarmYN.equals("Y") ? "Y" : "N";
        }
        //마케팅 알림
        if (marketingAlarmYN != null) {
            this.marketingAlarmYN = marketingAlarmYN.equals("Y") ? "Y" : "N";
        }
        this.updrId = userId;
    }

    /**
     * 가입여부 변경
     */
    public void updateTermYN(String termYN, Long userId) {
        this.termYN = termYN.equals("Y") ? "Y" : "N";
        this.updrId = userId;
    }

    /**
     * 회원탈퇴
     */
    public void softDelete(long userId) {
        this.delYn = "Y";
        this.updrId = userId;
    }
}
