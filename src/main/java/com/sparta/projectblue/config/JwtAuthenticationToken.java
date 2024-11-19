package com.sparta.projectblue.config;

import java.io.Serializable;
import java.util.Objects;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import com.sparta.projectblue.domain.common.dto.AuthUser;

public class JwtAuthenticationToken extends AbstractAuthenticationToken implements Serializable {

    private static final long serialVersionUID = 1L; // 직렬화 버전 ID 추가
    private final transient AuthUser authUser; // `transient` 키워드 추가

    public JwtAuthenticationToken(AuthUser authUser) {
        super(authUser.getAuthorities());
        this.authUser = authUser;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return authUser;
    }

    // equals 메서드 재정의
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JwtAuthenticationToken)) return false;
        if (!super.equals(o)) return false; // 상위 클래스의 equals 메서드 호출
        JwtAuthenticationToken that = (JwtAuthenticationToken) o;
        return Objects.equals(authUser, that.authUser);
    }

    // hashCode 메서드 재정의
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), authUser);
    }
}
