package com.sparta.projectblue.domain.performer.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.sparta.projectblue.domain.common.dto.AuthUser;
import com.sparta.projectblue.domain.common.enums.UserRole;
import com.sparta.projectblue.domain.performer.dto.CreatePerformerRequestDto;
import com.sparta.projectblue.domain.performer.dto.CreatePerformerResponseDto;
import com.sparta.projectblue.domain.performer.dto.UpdatePerformerRequestDto;
import com.sparta.projectblue.domain.performer.entity.Performer;
import com.sparta.projectblue.domain.performer.repository.PerformerRepository;

@ExtendWith(SpringExtension.class)
class PerformerAdminServiceTest {
    @Mock private PerformerRepository performerRepository;

    @InjectMocks private PerformerAdminService performerAdminService;

    @Test
    void 관리자_권한으로_배우를_생성한다() {
        // given
        AuthUser authUser = new AuthUser(1L, "admin@test.com", "adminUser", UserRole.ROLE_ADMIN);
        CreatePerformerRequestDto request =
                new CreatePerformerRequestDto("배두훈", "1986-07-15", "KOREA");

        Performer performer =
                new Performer(request.getName(), request.getBirth(), request.getNation());

        when(performerRepository.save(any(Performer.class))).thenReturn(performer);

        // when
        CreatePerformerResponseDto response = performerAdminService.create(authUser, request);

        // then
        assertNotNull(response);
        assertEquals("배두훈", response.getName());
        assertEquals("1986-07-15", response.getBirth());
        assertEquals("KOREA", response.getNation());
        assertTrue(
                authUser.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void 관리자권한이_없을_때_배우를_생성한다_예외처리() {
        // given
        AuthUser authUser = new AuthUser(1L, "User@test.com", "commonUser", UserRole.ROLE_USER);
        CreatePerformerRequestDto request =
                new CreatePerformerRequestDto("심영식", "1990-08-15", "KOREA");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> performerAdminService.create(authUser, request),
                        "예외가 발생해야 합니다.");

        // then
        assertEquals("관리자만 접근할 수 있습니다.", exception.getMessage());
    }

    @Test
    void 생일_형식이_잘못되었을_때_예외처리() {
        // given
        AuthUser authUser = new AuthUser(1L, "admin@test.com", "adminUser", UserRole.ROLE_ADMIN);
        CreatePerformerRequestDto request =
                new CreatePerformerRequestDto("홍길동", "20241031", "KOREA");

        // when & then
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> performerAdminService.create(authUser, request));
        assertEquals("생일은 yyyy-MM-dd 형식이어야 합니다.", exception.getMessage());
    }

    @Test
    void 생일이_유효하지_않은_날짜일_때_예외처리() {
        // given
        AuthUser authUser = new AuthUser(1L, "admin@test.com", "adminUser", UserRole.ROLE_ADMIN);
        CreatePerformerRequestDto request =
                new CreatePerformerRequestDto("홍길동", "1800-01-01", "KOREA");

        // when & then
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> performerAdminService.create(authUser, request));
        assertEquals("생일은 현재 날짜보다 이전이거나 1900년도 이후의 날짜여야 합니다.", exception.getMessage());
    }

    @Test
    void 관리자권한이_없을_때_배우_수정_예외처리() {
        // given
        AuthUser authUser = new AuthUser(1L, "user@test.com", "commonUser", UserRole.ROLE_USER);
        UpdatePerformerRequestDto request =
                new UpdatePerformerRequestDto("홍길동", "1990-01-01", "KOREA");

        // when & then
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> performerAdminService.update(authUser, 1L, request));
        assertEquals("관리자만 접근할 수 있습니다.", exception.getMessage());
    }

    @Test
    void 존재하지_않는_배우를_수정할_때_예외처리() {
        // given
        AuthUser authUser = new AuthUser(1L, "admin@test.com", "adminUser", UserRole.ROLE_ADMIN);
        UpdatePerformerRequestDto request =
                new UpdatePerformerRequestDto("홍길동", "1990-01-01", "KOREA");

        when(performerRepository.findById(any())).thenReturn(java.util.Optional.empty());

        // when & then
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> performerAdminService.update(authUser, 1L, request));
        assertEquals("배우를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void 관리자권한이_없을_때_배우_삭제_예외처리() {
        // given
        AuthUser authUser = new AuthUser(1L, "user@test.com", "commonUser", UserRole.ROLE_USER);

        // when & then
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> performerAdminService.delete(authUser, 1L));
        assertEquals("관리자만 접근할 수 있습니다.", exception.getMessage());
    }

    @Test
    void 존재하지_않는_배우를_삭제할_때_예외처리() {
        // given
        AuthUser authUser = new AuthUser(1L, "admin@test.com", "adminUser", UserRole.ROLE_ADMIN);

        when(performerRepository.findById(any())).thenReturn(java.util.Optional.empty());

        // when & then
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> performerAdminService.delete(authUser, 1L));
        assertEquals("해당 배우를 찾을 수 없습니다.", exception.getMessage());
    }
}
