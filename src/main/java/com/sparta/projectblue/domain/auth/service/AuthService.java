package com.sparta.projectblue.domain.auth.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.projectblue.aop.annotation.CouponLogstash;
import com.sparta.projectblue.config.JwtUtil;
import com.sparta.projectblue.domain.auth.dto.SigninRequestDto;
import com.sparta.projectblue.domain.auth.dto.SigninResponseDto;
import com.sparta.projectblue.domain.auth.dto.SignupRequestDto;
import com.sparta.projectblue.domain.auth.dto.SignupResponseDto;
import com.sparta.projectblue.domain.common.enums.CouponStatus;
import com.sparta.projectblue.domain.common.enums.CouponType;
import com.sparta.projectblue.domain.common.enums.UserRole;
import com.sparta.projectblue.domain.common.exception.AuthException;
import com.sparta.projectblue.domain.coupon.entity.Coupon;
import com.sparta.projectblue.domain.coupon.repository.CouponRepository;
import com.sparta.projectblue.domain.user.entity.User;
import com.sparta.projectblue.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;

    private final CouponRepository couponRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    @Transactional
    public SignupResponseDto signup(SignupRequestDto request) {

        validatePassword(request.getPassword());

        if (userRepository.existsByEmailAndIsDeletedTrue(request.getEmail())) {
            throw new IllegalArgumentException("탈퇴한 유저의 이메일은 재사용할 수 없습니다.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        UserRole userRole = UserRole.of(request.getUserRole());

        User newUser = new User(request.getEmail(), request.getName(), encodedPassword, userRole);
        User savedUser = userRepository.save(newUser);

        String bearerToken =
                jwtUtil.createToken(
                        savedUser.getId(), savedUser.getEmail(), savedUser.getName(), userRole);

        Coupon coupon = signupCoupon(); // 쿠폰 생성

        return new SignupResponseDto(bearerToken, coupon); // 응답시 쿠폰 정보 포함
    }

    public SigninResponseDto signin(SigninRequestDto request) {

        User user =
                userRepository
                        .findByEmail(request.getEmail())
                        .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 유저입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthException("잘못된 비밀번호입니다.");
        }

        String bearerToken =
                jwtUtil.createToken(
                        user.getId(), user.getEmail(), user.getName(), user.getUserRole());

        return new SigninResponseDto(bearerToken);
    }

    private void validatePassword(String password) {

        if (password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 최소 8자 이상이어야 합니다.");
        }
        if (!password.matches(".*[a-zA-Z].*")) { // 대문자 또는 소문자 포함
            throw new IllegalArgumentException("비밀번호는 대문자 또는 소문자를 포함해야 합니다.");
        }
        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("비밀번호는 숫자를 포함해야 합니다.");
        }
        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            throw new IllegalArgumentException("비밀번호는 특수문자를 포함해야 합니다.");
        }
    }

    // 쿠폰 생성 메서드
    @CouponLogstash
    private Coupon signupCoupon() {

        Coupon coupon =
                new Coupon(
                        "신규고객쿠폰 5000원",
                        CouponType.AMOUNT,
                        CouponStatus.ACTIVE,
                        1,
                        0,
                        5000L,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(30));
        couponRepository.save(coupon);

        return coupon;
    }
}
