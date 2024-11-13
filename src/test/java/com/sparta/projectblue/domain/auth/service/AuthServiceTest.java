package com.sparta.projectblue.domain.auth.service;

import com.sparta.projectblue.config.JwtUtil;
import com.sparta.projectblue.domain.auth.dto.SigninRequestDto;
import com.sparta.projectblue.domain.auth.dto.SigninResponseDto;
import com.sparta.projectblue.domain.auth.dto.SignupRequestDto;
import com.sparta.projectblue.domain.auth.dto.SignupResponseDto;
import com.sparta.projectblue.domain.common.enums.UserRole;
import com.sparta.projectblue.domain.common.exception.AuthException;
import com.sparta.projectblue.domain.coupon.entity.Coupon;
import com.sparta.projectblue.domain.coupon.repository.CouponRepository;
import com.sparta.projectblue.domain.user.entity.User;
import com.sparta.projectblue.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Nested
    class SignupTest {

        @Test
        void 회원가입_정상_동작() {

            // given
            User user = new User("email", "$2a$04$jfQeXoc7b5IWWvZFPDE.he56RmITYyjnPA4haWZB2EgFda9uDXsHC", "pwd", UserRole.ROLE_USER);

            long userId = 1L;

            ReflectionTestUtils.setField(user, "id", userId);

            String bearerToken = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0IiwiZW1haWwiOiJ1c2VyNEBtYWlsLmNvbSIsInVzZXJSb2xlIjoiVVNFUiIsImV4cCI6MTcyNjA0NDU4NCwiaWF0IjoxNzI2MDQwOTg0fQ.jaMMtapDd9DZcTmztJv-zk2vpUGxge9UiFhxCCt7KfE";

            given(userRepository.existsByEmail(anyString())).willReturn(false);

            given(userRepository.save(any(User.class))).willReturn(user);

            given(jwtUtil.createToken(anyLong(), anyString(), anyString(), any(UserRole.class))).willReturn(bearerToken);

            Coupon coupon = new Coupon();

            given(couponRepository.save(any(Coupon.class))).willReturn(coupon);

            // when
            SignupRequestDto request = new SignupRequestDto("email", "abc123?!", "pwd", "ROLE_USER");

            SignupResponseDto response = authService.signup(request);

            // then
            assertNotNull(response.getBearerToken());

            assertEquals(bearerToken, response.getBearerToken());
        }

        @Test
        void 탈퇴한_이메일_오류() {

            // given
            given(userRepository.existsByEmailAndIsDeletedTrue(anyString())).willReturn(true);

            // when
            SignupRequestDto request = new SignupRequestDto("email", "abc123?!", "pwd", "ROLE_USER");

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    authService.signup(request));

            // then
            assertEquals(exception.getMessage(), "탈퇴한 유저의 이메일은 재사용할 수 없습니다.");
        }

        @Test
        void 중복_이메일_오류() {

            // given
            given(userRepository.existsByEmail(anyString())).willReturn(true);

            // when
            SignupRequestDto request = new SignupRequestDto("email", "abc123?!", "pwd", "ROLE_USER");

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    authService.signup(request));

            // then
            assertEquals(exception.getMessage(), "이미 존재하는 이메일입니다.");
        }

        @Test
        void 비밀번호_길이_오류() {

            // given
            SignupRequestDto request = new SignupRequestDto("email", "a", "pwd", "ROLE_USER");

            // when
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    authService.signup(request));

            // then
            assertEquals(exception.getMessage(), "비밀번호는 최소 8자 이상이어야 합니다.");
        }

        @Test
        void 비밀번호_대소문자_오류() {

            // given
            SignupRequestDto request = new SignupRequestDto("email", "1111????", "pwd", "ROLE_USER");

            // when
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    authService.signup(request));

            // then
            assertEquals(exception.getMessage(), "비밀번호는 대문자 또는 소문자를 포함해야 합니다.");
        }

        @Test
        void 비밀번호_숫자_오류() {

            // given
            SignupRequestDto request = new SignupRequestDto("email", "abc?????", "pwd", "ROLE_USER");

            // when
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    authService.signup(request));

            // then
            assertEquals(exception.getMessage(), "비밀번호는 숫자를 포함해야 합니다.");
        }

        @Test
        void 비밀번호_특수문자_오류() {

            // given
            SignupRequestDto request = new SignupRequestDto("email", "abc12345", "pwd", "ROLE_USER");

            // when
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    authService.signup(request));

            // then
            assertEquals(exception.getMessage(), "비밀번호는 특수문자를 포함해야 합니다.");
        }
    }

    @Nested
    class SigninTest {

        @Test
        void 로그인_정상_동작() {

            // given
            SigninRequestDto request = new SigninRequestDto("email", "pwd");

            User user = new User("email", "$2a$04$jfQeXoc7b5IWWvZFPDE.he56RmITYyjnPA4haWZB2EgFda9uDXsHC", "pwd", UserRole.ROLE_USER);

            long userId = 1L;

            ReflectionTestUtils.setField(user, "id", userId);

            String bearerToken = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0IiwiZW1haWwiOiJ1c2VyNEBtYWlsLmNvbSIsInVzZXJSb2xlIjoiVVNFUiIsImV4cCI6MTcyNjA0NDU4NCwiaWF0IjoxNzI2MDQwOTg0fQ.jaMMtapDd9DZcTmztJv-zk2vpUGxge9UiFhxCCt7KfE";

            given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));

            given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);

            given(jwtUtil.createToken(anyLong(), anyString(), anyString(), any(UserRole.class))).willReturn(bearerToken);

            // when
            SigninResponseDto response = authService.signin(request);

            // then
            assertNotNull(response.getBearerToken());

            assertEquals(bearerToken, response.getBearerToken());
        }

        @Test
        void 잘못된_비밀번호_오류() {

            // given
            SigninRequestDto request = new SigninRequestDto("email", "pwd");

            User user = new User("email", "$2a$04$jfQeXoc7b5IWWvZFPDE.he56RmITYyjnPA4haWZB2EgFda9uDXsHC", "pwd", UserRole.ROLE_USER);

            long userId = 1L;

            ReflectionTestUtils.setField(user, "id", userId);

            given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));

            given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

            // when
            AuthException exception = assertThrows(AuthException.class, () ->
                    authService.signin(request));

            // then
            assertEquals(exception.getMessage(), "잘못된 비밀번호입니다.");
        }
    }
}
