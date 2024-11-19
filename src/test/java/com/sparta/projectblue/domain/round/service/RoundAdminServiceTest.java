package com.sparta.projectblue.domain.round.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.sparta.projectblue.domain.common.dto.AuthUser;
import com.sparta.projectblue.domain.common.enums.PerformanceStatus;
import com.sparta.projectblue.domain.common.enums.UserRole;
import com.sparta.projectblue.domain.performance.entity.Performance;
import com.sparta.projectblue.domain.performance.repository.PerformanceRepository;
import com.sparta.projectblue.domain.round.dto.CreateRoundRequestDto;
import com.sparta.projectblue.domain.round.dto.CreateRoundResponseDto;
import com.sparta.projectblue.domain.round.dto.UpdateRoundRequestDto;
import com.sparta.projectblue.domain.round.dto.UpdateRoundResponseDto;
import com.sparta.projectblue.domain.round.entity.Round;
import com.sparta.projectblue.domain.round.repository.RoundRepository;
import com.sparta.projectblue.domain.user.entity.User;
import com.sparta.projectblue.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class RoundAdminServiceTest {

    @Mock private RoundRepository roundRepository;

    @Mock private PerformanceRepository performanceRepository;

    @Mock private UserRepository userRepository;

    @InjectMocks private RoundAdminService roundAdminService;

    @Test
    void 유효한_요청으로_회차를_정상_생성하는_테스트() {
        // given
        AuthUser authUser = new AuthUser(1L, "admin@example.com", "Admin", UserRole.ROLE_ADMIN);
        Long performanceId = 1L;
        LocalDateTime futureDate = LocalDateTime.now().plusDays(1);
        CreateRoundRequestDto request =
                new CreateRoundRequestDto(performanceId, Collections.singletonList(futureDate));
        Performance performance = new Performance();

        Round round = new Round(performanceId, futureDate, PerformanceStatus.BEFORE_OPEN);
        ReflectionTestUtils.setField(round, "id", 1L);

        User adminUser = new User();
        ReflectionTestUtils.setField(adminUser, "id", authUser.getId());
        ReflectionTestUtils.setField(adminUser, "userRole", UserRole.ROLE_ADMIN);

        when(userRepository.findById(authUser.getId())).thenReturn(Optional.of(adminUser));
        when(performanceRepository.findById(performanceId)).thenReturn(Optional.of(performance));
        when(roundRepository.saveAll(any())).thenReturn(List.of(round));

        // when
        List<CreateRoundResponseDto> response =
                roundAdminService.create(authUser, performanceId, request);

        // then
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).getId());
        assertEquals(performanceId, response.get(0).getPerformanceId());
        assertEquals(futureDate, response.get(0).getDate());
        assertEquals(PerformanceStatus.BEFORE_OPEN, response.get(0).getStatus());
    }

    @Test
    void 회차_생성_시_공연이_없을_때_예외_발생_테스트() {
        // given
        AuthUser authUser = new AuthUser(1L, "admin@example.com", "Admin", UserRole.ROLE_ADMIN);
        Long performanceId = 1L;
        LocalDateTime futureDate = LocalDateTime.now().plusDays(1);
        CreateRoundRequestDto request =
                new CreateRoundRequestDto(performanceId, Collections.singletonList(futureDate));

        // Mock User 설정
        User adminUser = new User();
        ReflectionTestUtils.setField(adminUser, "id", authUser.getId());
        ReflectionTestUtils.setField(adminUser, "userRole", UserRole.ROLE_ADMIN);

        when(userRepository.findById(authUser.getId())).thenReturn(Optional.of(adminUser));
        when(performanceRepository.findById(performanceId)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> roundAdminService.create(authUser, performanceId, request));
        assertEquals("공연을 찾을 수 없습니다", exception.getMessage());
    }

    @Test
    void 과거_날짜로_회차_생성_시_예외_발생_테스트() {
        // given
        AuthUser authUser = new AuthUser(1L, "admin@example.com", "Admin", UserRole.ROLE_ADMIN);

        // Mock User 설정
        User adminUser = new User();
        ReflectionTestUtils.setField(adminUser, "id", authUser.getId());
        ReflectionTestUtils.setField(adminUser, "userRole", UserRole.ROLE_ADMIN);

        when(userRepository.findById(authUser.getId())).thenReturn(Optional.of(adminUser));

        Long performanceId = 1L;
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
        CreateRoundRequestDto request =
                new CreateRoundRequestDto(performanceId, Collections.singletonList(pastDate));

        when(performanceRepository.findById(performanceId))
                .thenReturn(Optional.of(new Performance()));

        // when & then
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> roundAdminService.create(authUser, performanceId, request));
        assertEquals("과거의 날짜로 회차를 생성할 수 없습니다.", exception.getMessage());
    }

    @Test
    void 수정_시_회차가_없을_때_예외_발생_테스트() {
        // given
        AuthUser authUser = new AuthUser(1L, "admin@example.com", "Admin", UserRole.ROLE_ADMIN);
        Long roundId = 1L;
        LocalDateTime futureDate = LocalDateTime.now().plusDays(1);
        PerformanceStatus status = PerformanceStatus.BEFORE_OPEN;
        UpdateRoundRequestDto request = new UpdateRoundRequestDto(futureDate, status);

        User adminUser = new User();
        ReflectionTestUtils.setField(adminUser, "id", authUser.getId());
        ReflectionTestUtils.setField(adminUser, "userRole", UserRole.ROLE_ADMIN);

        when(userRepository.findById(authUser.getId())).thenReturn(Optional.of(adminUser));

        when(roundRepository.findById(roundId)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> roundAdminService.update(authUser, roundId, request));
        assertEquals("회차를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void 과거_날짜로_회차_수정_시_예외_발생_테스트() {
        // given
        AuthUser authUser = new AuthUser(1L, "admin@example.com", "Admin", UserRole.ROLE_ADMIN);
        Long roundId = 1L;
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
        Round existingRound =
                new Round(1L, LocalDateTime.now().plusDays(1), PerformanceStatus.BEFORE_OPEN);

        User adminUser = new User();
        ReflectionTestUtils.setField(adminUser, "id", authUser.getId());
        ReflectionTestUtils.setField(adminUser, "userRole", UserRole.ROLE_ADMIN);

        when(userRepository.findById(authUser.getId())).thenReturn(Optional.of(adminUser));

        when(roundRepository.findById(roundId)).thenReturn(Optional.of(existingRound));
        UpdateRoundRequestDto request =
                new UpdateRoundRequestDto(pastDate, PerformanceStatus.AVAILABLE);

        // when & then
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> roundAdminService.update(authUser, roundId, request));
        assertEquals("과거의 날짜로 회차를 수정할 수 없습니다.", exception.getMessage());
    }

    @Test
    void date_만_제공된_경우_업데이트_테스트() {
        // given
        AuthUser authUser = new AuthUser(1L, "admin@example.com", "Admin", UserRole.ROLE_ADMIN);
        Long roundId = 1L;
        LocalDateTime newDate = LocalDateTime.now().plusDays(3);
        Round existingRound =
                new Round(1L, LocalDateTime.now().plusDays(1), PerformanceStatus.BEFORE_OPEN);

        User adminUser = new User();
        ReflectionTestUtils.setField(adminUser, "id", authUser.getId());
        ReflectionTestUtils.setField(adminUser, "userRole", UserRole.ROLE_ADMIN);

        when(userRepository.findById(authUser.getId())).thenReturn(Optional.of(adminUser));
        when(roundRepository.findById(roundId)).thenReturn(Optional.of(existingRound));
        UpdateRoundRequestDto request = new UpdateRoundRequestDto(newDate, null);

        // when
        UpdateRoundResponseDto response = roundAdminService.update(authUser, roundId, request);

        // then
        assertEquals(newDate, response.getDate());
        assertEquals(existingRound.getStatus(), response.getStatus());
    }

    @Test
    void status_만_제공된_경우_업데이트_테스트() {
        // given
        AuthUser authUser = new AuthUser(1L, "admin@example.com", "Admin", UserRole.ROLE_ADMIN);
        Long roundId = 1L;
        PerformanceStatus newStatus = PerformanceStatus.AVAILABLE;
        Round existingRound =
                new Round(1L, LocalDateTime.now().plusDays(1), PerformanceStatus.BEFORE_OPEN);

        User adminUser = new User();
        ReflectionTestUtils.setField(adminUser, "id", authUser.getId());
        ReflectionTestUtils.setField(adminUser, "userRole", UserRole.ROLE_ADMIN);

        when(userRepository.findById(authUser.getId())).thenReturn(Optional.of(adminUser));
        when(roundRepository.findById(roundId)).thenReturn(Optional.of(existingRound));
        UpdateRoundRequestDto request = new UpdateRoundRequestDto(null, newStatus);

        // when
        UpdateRoundResponseDto response = roundAdminService.update(authUser, roundId, request);

        // then
        assertEquals(existingRound.getDate(), response.getDate());
        assertEquals(newStatus, response.getStatus());
    }

    @Test
    void date와_status_모두_제공된_경우_업데이트_테스트() {
        // given
        AuthUser authUser = new AuthUser(1L, "admin@example.com", "Admin", UserRole.ROLE_ADMIN);
        Long roundId = 1L;
        LocalDateTime newDate = LocalDateTime.now().plusDays(3);
        PerformanceStatus newStatus = PerformanceStatus.AVAILABLE;
        Round existingRound =
                new Round(1L, LocalDateTime.now().plusDays(1), PerformanceStatus.BEFORE_OPEN);

        User adminUser = new User();
        ReflectionTestUtils.setField(adminUser, "id", authUser.getId());
        ReflectionTestUtils.setField(adminUser, "userRole", UserRole.ROLE_ADMIN);

        when(userRepository.findById(authUser.getId())).thenReturn(Optional.of(adminUser));
        when(roundRepository.findById(roundId)).thenReturn(Optional.of(existingRound));
        UpdateRoundRequestDto request = new UpdateRoundRequestDto(newDate, newStatus);

        // when
        UpdateRoundResponseDto response = roundAdminService.update(authUser, roundId, request);

        // then
        assertEquals(newDate, response.getDate());
        assertEquals(newStatus, response.getStatus());
    }

    @Test
    void date와_status_모두_null_인_경우_기존값_유지_테스트() {
        // given
        AuthUser authUser = new AuthUser(1L, "admin@example.com", "Admin", UserRole.ROLE_ADMIN);
        Long roundId = 1L;
        Round existingRound =
                new Round(1L, LocalDateTime.now().plusDays(1), PerformanceStatus.BEFORE_OPEN);

        User adminUser = new User();
        ReflectionTestUtils.setField(adminUser, "id", authUser.getId());
        ReflectionTestUtils.setField(adminUser, "userRole", UserRole.ROLE_ADMIN);

        when(userRepository.findById(authUser.getId())).thenReturn(Optional.of(adminUser));
        when(roundRepository.findById(roundId)).thenReturn(Optional.of(existingRound));
        UpdateRoundRequestDto request = new UpdateRoundRequestDto(null, null);

        // when
        UpdateRoundResponseDto response = roundAdminService.update(authUser, roundId, request);

        // then
        assertEquals(existingRound.getDate(), response.getDate());
        assertEquals(existingRound.getStatus(), response.getStatus());
    }

    @Test
    void 회차가_없을_때_삭제_예외_발생_테스트() {
        // given
        AuthUser authUser = new AuthUser(1L, "admin@example.com", "Admin", UserRole.ROLE_ADMIN);
        User adminUser = new User();
        ReflectionTestUtils.setField(adminUser, "id", authUser.getId());
        ReflectionTestUtils.setField(adminUser, "userRole", UserRole.ROLE_ADMIN);
        Long roundId = 1L;
        when(userRepository.findById(authUser.getId())).thenReturn(Optional.of(adminUser));
        when(roundRepository.findById(roundId)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> roundAdminService.delete(authUser, roundId));
        assertEquals("회차를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void 유효한_회차_삭제_테스트() {
        // given
        AuthUser authUser = new AuthUser(1L, "admin@example.com", "Admin", UserRole.ROLE_ADMIN);
        User adminUser = new User();
        ReflectionTestUtils.setField(adminUser, "id", authUser.getId());
        ReflectionTestUtils.setField(adminUser, "userRole", UserRole.ROLE_ADMIN);
        Long roundId = 1L;
        Round existingRound =
                new Round(roundId, LocalDateTime.now().plusDays(1), PerformanceStatus.BEFORE_OPEN);

        when(userRepository.findById(authUser.getId())).thenReturn(Optional.of(adminUser));
        when(roundRepository.findById(roundId)).thenReturn(Optional.of(existingRound));

        // when
        roundAdminService.delete(authUser, roundId);

        // then
        verify(roundRepository, times(1)).delete(existingRound);
    }

    @Test
    void 중복된_날짜로_회차_생성_시_예외_발생_테스트() {
        AuthUser authUser = new AuthUser(1L, "admin@example.com", "Admin", UserRole.ROLE_ADMIN);
        LocalDateTime now = LocalDateTime.now().plusDays(1);
        CreateRoundRequestDto request = new CreateRoundRequestDto(1L, List.of(now, now));

        User adminUser = new User();
        ReflectionTestUtils.setField(adminUser, "id", authUser.getId());
        ReflectionTestUtils.setField(adminUser, "userRole", UserRole.ROLE_ADMIN);

        when(userRepository.findById(authUser.getId())).thenReturn(Optional.of(adminUser));
        when(performanceRepository.findById(1L)).thenReturn(Optional.of(new Performance()));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> roundAdminService.create(authUser, 1L, request));
        assertEquals("요청 내 날짜들이 중복될 수 없습니다.", exception.getMessage());
    }

    @Test
    void 기존_회차와_1시간_이상_차이로_생성_시_정상_동작_테스트() {
        // given
        AuthUser authUser = new AuthUser(1L, "admin@example.com", "Admin", UserRole.ROLE_ADMIN);
        Long performanceId = 1L;

        // 이미 존재하는 회차의 날짜 설정
        LocalDateTime existingDate = LocalDateTime.now().plusDays(1);
        Round existingRound = new Round(performanceId, existingDate, PerformanceStatus.BEFORE_OPEN);

        User adminUser = new User();
        ReflectionTestUtils.setField(adminUser, "id", authUser.getId());
        ReflectionTestUtils.setField(adminUser, "userRole", UserRole.ROLE_ADMIN);

        // 새로 생성하려는 회차의 날짜를 기존 회차와 1시간 이상 차이로 설정
        LocalDateTime newDate = existingDate.plusHours(2);
        CreateRoundRequestDto request = new CreateRoundRequestDto(performanceId, List.of(newDate));

        when(userRepository.findById(authUser.getId())).thenReturn(Optional.of(adminUser));
        when(performanceRepository.findById(performanceId))
                .thenReturn(Optional.of(new Performance()));
        when(roundRepository.findByPerformanceId(performanceId)).thenReturn(List.of(existingRound));
        when(roundRepository.saveAll(any()))
                .thenReturn(
                        List.of(new Round(performanceId, newDate, PerformanceStatus.BEFORE_OPEN)));

        // when
        List<CreateRoundResponseDto> response =
                roundAdminService.create(authUser, performanceId, request);

        // then
        assertEquals(1, response.size());
        assertEquals(newDate, response.get(0).getDate());
    }

    @Test
    void 기존_회차와_1시간_미만_차이로_생성_시_예외_발생_테스트() {
        // given
        AuthUser authUser = new AuthUser(1L, "admin@example.com", "Admin", UserRole.ROLE_ADMIN);
        Long performanceId = 1L;

        // 이미 존재하는 회차의 날짜 설정 (예: 현재 시간 기준으로 1시간 전)
        LocalDateTime existingDate = LocalDateTime.now().plusDays(1);
        Round existingRound = new Round(performanceId, existingDate, PerformanceStatus.BEFORE_OPEN);

        User adminUser = new User();
        ReflectionTestUtils.setField(adminUser, "id", authUser.getId());
        ReflectionTestUtils.setField(adminUser, "userRole", UserRole.ROLE_ADMIN);

        // 새로 생성하려는 회차의 날짜를 기존 회차와 30분 차이로 설정
        LocalDateTime newDate = existingDate.plusMinutes(30);
        CreateRoundRequestDto request = new CreateRoundRequestDto(performanceId, List.of(newDate));

        when(userRepository.findById(authUser.getId())).thenReturn(Optional.of(adminUser));
        when(performanceRepository.findById(performanceId))
                .thenReturn(Optional.of(new Performance()));
        when(roundRepository.findByPerformanceId(performanceId)).thenReturn(List.of(existingRound));

        // when & then
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> roundAdminService.create(authUser, performanceId, request));
        assertEquals("기존 회차와 1시간 이상 차이가 나야 합니다.", exception.getMessage());
    }
}
