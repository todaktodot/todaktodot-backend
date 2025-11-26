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

    @Enumerated(EnumType.STRING)
    private Role role;

    private String provider; // google, kakao, apple
    private String providerId;

    @Builder
    public User(String email, String name, Role role, String provider, String providerId) {
        this.email = email;
        this.name = name;
        this.role = role;
        this.provider = provider;
        this.providerId = providerId;
    }
}
