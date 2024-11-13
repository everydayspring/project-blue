package com.sparta.projectblue.domain.user.service;

import com.sparta.projectblue.domain.common.dto.AuthUser;
import com.sparta.projectblue.domain.common.enums.UserRole;
import com.sparta.projectblue.domain.user.dto.DeleteUserRequestDto;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    @Nested
    class DeleteTest {

        @Test
        void 탈퇴_정상_동작() {

            // given
            AuthUser authUser = new AuthUser(1L, "test@test.com", "testUser", UserRole.ROLE_USER);

            User user = new User(authUser.getEmail(), authUser.getName(), "abc132?!", UserRole.ROLE_USER);

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

            given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);

            // when
            DeleteUserRequestDto request = new DeleteUserRequestDto("pass123?!");

            userService.delete(authUser, request);

            // then
            assertTrue(user.isDeleted());
        }

        @Test
        void 이미_탈퇴한_유저_오류() {

            // given
            AuthUser authUser = new AuthUser(1L, "test@test.com", "testUser", UserRole.ROLE_USER);

            User user = new User(authUser.getEmail(), authUser.getName(), "abc132?!", UserRole.ROLE_USER);
            ReflectionTestUtils.setField(user, "isDeleted", true);

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

            // when
            DeleteUserRequestDto request = new DeleteUserRequestDto("pass123?!");

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    userService.delete(authUser, request));

            // then
            assertEquals(exception.getMessage(), "이미 탈퇴한 유저입니다.");
        }

        @Test
        void 잘못된_비밀번호_오류() {

            // given
            AuthUser authUser = new AuthUser(1L, "test@test.com", "testUser", UserRole.ROLE_USER);

            User user = new User(authUser.getEmail(), authUser.getName(), "abc132?!", UserRole.ROLE_USER);

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

            given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

            // when
            DeleteUserRequestDto request = new DeleteUserRequestDto("pass123?!");

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    userService.delete(authUser, request));

            // then
            assertEquals(exception.getMessage(), "비밀번호가 일치하지 않습니다.");
        }
    }
}
