package com.sparta.projectblue.domain.auth.controller;

import com.sparta.projectblue.config.ApiResponse;
import com.sparta.projectblue.domain.auth.service.KakaoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class KakaoController {

    private final KakaoService kakaoService;

    // 카카오 로그인 요청
    @GetMapping("/kakao")
    public String kakaoConnect() {
        return kakaoService.kakaoLogin();
    }

    @GetMapping("/kakaoCallback")
    public ResponseEntity<ApiResponse<String>> kakaoSignin(HttpServletRequest request) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(kakaoService.callback(request.getParameter("code"))));
    }
}
