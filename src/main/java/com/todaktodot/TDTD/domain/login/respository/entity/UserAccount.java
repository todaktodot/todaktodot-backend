package com.todaktodot.TDTD.domain.login.respository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@Table(
    name = "USER_ACCOUNT",
    uniqueConstraints = {
            @UniqueConstraint(columnNames = {"PROVIDER", "PROVIDER_ID"})
    }
)
public class UserAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "USER_EMAIL")
    private String email;

    @Column(name = "USER_NAME")
    private String name;

    @Column(name = "PROVIDER")
    private String provider; // GOOGLE, KAKAO, APPLE

    @Column(name = "PROVIDER_ID", nullable = false)
    private String providerId;

    @Column(name = "REFRESH_TOKEN")
    private String refreshToken;

    @Column(name = "REG_DT", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime regDt;

    @Column(name = "REGR_ID", columnDefinition = "BIGINT")
    private Long regrId;

    @Column(name = "DEL_YN", nullable = false, length = 1)
    @Builder.Default
    private String delYn = "N";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    //연관관계 설정
    public void setUser(User user) {
        this.user = user;
        this.regrId = user.getId();
    }

    //리프레쉬 토큰 저장
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    //회원탈퇴
    public void softDelete(long userId) {
        this.delYn = "Y";
    }
}
