package com.sparta.projectblue.domain.auth.dto;

import lombok.Getter;

@Getter
public class SignupResponseDto {
    private final String bearerToken;

    public SignupResponseDto(String bearerToken) {
        this.bearerToken = bearerToken;
    }
}
