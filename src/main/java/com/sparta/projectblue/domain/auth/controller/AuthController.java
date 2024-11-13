package com.sparta.projectblue.domain.auth.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.projectblue.domain.auth.dto.SigninRequestDto;
import com.sparta.projectblue.domain.auth.dto.SigninResponseDto;
import com.sparta.projectblue.domain.auth.dto.SignupRequestDto;
import com.sparta.projectblue.domain.auth.dto.SignupResponseDto;
import com.sparta.projectblue.domain.auth.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "User-Auth", description = "회원가입 로그인 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/auth/signup")
    @Operation(summary = "회원가입")
    public SignupResponseDto signup(@Valid @RequestBody SignupRequestDto request) {

        return authService.signup(request);
    }

    @PostMapping("/auth/signin")
    @Operation(summary = "로그인", description = "testuser password : abc123?!")
    public SigninResponseDto signin(@Valid @RequestBody SigninRequestDto request) {

        return authService.signin(request);
    }
}
