package com.todaktodot.TDTD.domain.login.respository.entity;

import com.todaktodot.TDTD.global.security.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String name;

    @Column(length = 20)
    private String nickname;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String provider; // google, kakao, apple
    private String providerId;

    @Builder
    public User(String email, String name, String nickname, Role role, String provider, String providerId) {
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.role = role;
        this.provider = provider;
        this.providerId = providerId;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}
