package com.sparta.projectblue.domain.user.entity;

import jakarta.persistence.*;

import com.sparta.projectblue.domain.common.entity.BaseEntity;
import com.sparta.projectblue.domain.common.enums.UserRole;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "users")
public class User extends BaseEntity {

    @Column(unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 60)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, name = "user_role")
    private UserRole userRole;

    @Column(name = "kakao_id")
    private Long kakaoId;

    @Column(name = "slack_id")
    private String slackId;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    public User(String email, String name, String password, UserRole userRole) {

        this.email = email;
        this.name = name;
        this.password = password;
        this.userRole = userRole;
    }

    public User(String email, String name, String password, UserRole userRole, Long kakaoId) {

        this.email = email;
        this.name = name;
        this.password = password;
        this.userRole = userRole;
        this.kakaoId = kakaoId;
    }

    public void userDeleted() {

        this.isDeleted = true;
    }

    public void insertKakaoId(Long kakaoId) {

        this.kakaoId = kakaoId;
    }
}
