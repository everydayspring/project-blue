package com.sparta.projectblue.config;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import com.sparta.projectblue.domain.common.dto.AuthUser;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final AuthUser authUser;

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
}
