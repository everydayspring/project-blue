package com.sparta.projectblue.domain.auth.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.projectblue.config.ApiResponse;
import com.sparta.projectblue.domain.auth.service.KakaoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "User-Kakao", description = "카카오 회원가입 로그인 API")
public class KakaoController {

    private final KakaoService kakaoService;

    @GetMapping("/kakao")
    @Operation(summary = "연동")
    public String kakaoConnect() {

        return kakaoService.kakaoLogin();
    }

    @GetMapping("/kakaoCallback")
    @Operation(summary = "로그인")
    public ResponseEntity<ApiResponse<String>> kakaoSignin(HttpServletRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(kakaoService.callback(request.getParameter("code"))));
    }
}
