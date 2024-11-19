package com.sparta.projectblue.domain.round.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.projectblue.domain.common.dto.AuthUser;
import com.sparta.projectblue.domain.common.enums.PerformanceStatus;
import com.sparta.projectblue.domain.common.enums.UserRole;
import com.sparta.projectblue.domain.performance.repository.PerformanceRepository;
import com.sparta.projectblue.domain.round.dto.CreateRoundRequestDto;
import com.sparta.projectblue.domain.round.dto.CreateRoundResponseDto;
import com.sparta.projectblue.domain.round.dto.UpdateRoundRequestDto;
import com.sparta.projectblue.domain.round.dto.UpdateRoundResponseDto;
import com.sparta.projectblue.domain.round.entity.Round;
import com.sparta.projectblue.domain.round.repository.RoundRepository;
import com.sparta.projectblue.domain.user.entity.User;
import com.sparta.projectblue.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RoundAdminService {

    private final RoundRepository roundRepository;
    private final PerformanceRepository performanceRepository;
    private final UserRepository userRepository;

    // 1시간 이상 차이 검증 메서드 (새 회차 추가용)
    private void validateTimeDifferenceForNewRound(Long performanceId, LocalDateTime newDate) {
        List<Round> existingRounds = roundRepository.findByPerformanceId(performanceId);

        for (Round existingRound : existingRounds) {
            LocalDateTime existingDate = existingRound.getDate();
            Duration duration = Duration.between(existingDate, newDate).abs();

            // 1시간 이하의 차이인 경우 예외 발생
            if (duration.toHours() < 1 && duration.toMinutes() < 60) {
                throw new IllegalArgumentException("기존 회차와 1시간 이상 차이가 나야 합니다.");
            }
        }
    }

    @Transactional
    public List<CreateRoundResponseDto> create(
            AuthUser authUser, Long id, CreateRoundRequestDto request) {
        // 권한 확인
        hasRole(authUser);

        // 공연 존재 여부 확인
        if (performanceRepository.findById(id).isEmpty()) {
            throw new IllegalArgumentException("공연을 찾을 수 없습니다");
        }

        LocalDateTime now = LocalDateTime.now();

        // 요청 날짜 내 중복 검증
        List<LocalDateTime> dates = request.getDates();
        Set<LocalDateTime> distinctDates = new HashSet<>(dates);
        if (dates.size() != distinctDates.size()) {
            throw new IllegalArgumentException("요청 내 날짜들이 중복될 수 없습니다.");
        }

        // 요청된 각 날짜에 대해 검증 수행
        for (LocalDateTime date : dates) {
            // 과거 날짜 확인
            if (date.isBefore(now)) {
                throw new IllegalArgumentException("과거의 날짜로 회차를 생성할 수 없습니다.");
            }

            // 기존 회차와 1시간 이상 차이 검증
            validateTimeDifferenceForNewRound(id, date);
        }

        // 새로운 회차 생성
        List<Round> newRounds = new ArrayList<>();
        for (LocalDateTime date : dates) {
            newRounds.add(new Round(id, date, PerformanceStatus.BEFORE_OPEN));
        }

        // 저장된 회차 목록 반환
        List<Round> savedRounds = roundRepository.saveAll(newRounds);

        // CreateRoundResponseDto로 매핑
        List<CreateRoundResponseDto> responseDtos = new ArrayList<>();
        for (Round savedRound : savedRounds) {
            responseDtos.add(new CreateRoundResponseDto(savedRound));
        }

        return responseDtos;
    }

    // 1시간 이상 차이 검증 메서드 (수정 중인 회차 제외)
    private void validateTimeDifferenceForExistingRound(
            Long performanceId, LocalDateTime newDate, Long roundId) {
        List<Round> existingRounds = roundRepository.findByPerformanceId(performanceId);

        for (Round existingRound : existingRounds) {
            if (existingRound.getId().equals(roundId)) {
                continue;
            }

            LocalDateTime existingDate = existingRound.getDate();
            Duration duration = Duration.between(existingDate, newDate).abs();

            // 1시간 이하의 차이인 경우 예외 발생
            if (duration.toHours() < 1 && duration.toMinutes() < 60) {
                throw new IllegalArgumentException("기존 회차와 1시간 이상 차이가 나야 합니다.");
            }
        }
    }

    @Transactional
    public UpdateRoundResponseDto update(
            AuthUser authUser, Long id, UpdateRoundRequestDto request) {
        // 권한 확인
        hasRole(authUser);

        // 회차 가져오기
        Round round =
                roundRepository
                        .findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("회차를 찾을 수 없습니다."));

        LocalDateTime now = LocalDateTime.now();

        if (request.getDate() != null) {
            if (request.getDate().isBefore(now)) {
                throw new IllegalArgumentException("과거의 날짜로 회차를 수정할 수 없습니다.");
            }

            // 기존 회차 시간과 다를 때만 1시간 이상 차이 검증
            if (!round.getDate().equals(request.getDate())) {
                validateTimeDifferenceForExistingRound(
                        round.getPerformanceId(), request.getDate(), round.getId());
            }

            round.updateDate(request.getDate());
        } else {
            request = new UpdateRoundRequestDto(round.getDate(), request.getStatus());
        }

        PerformanceStatus newStatus =
                request.getStatus() != null ? request.getStatus() : round.getStatus();
        round.updateStatus(newStatus);

        roundRepository.save(round);

        return new UpdateRoundResponseDto(round);
    }

    @Transactional
    public void delete(AuthUser authUser, Long id) {

        // 권한 확인
        hasRole(authUser);

        Round round =
                roundRepository
                        .findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("회차를 찾을 수 없습니다."));

        roundRepository.delete(round);
    }

    private void hasRole(AuthUser authUser) {
        if (!authUser.hasRole(UserRole.ROLE_ADMIN)) {
            throw new IllegalArgumentException("관리자만 접근할 수 있습니다.");
        }

        User user =
                userRepository
                        .findById(authUser.getId())
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        if (user.getUserRole() != UserRole.ROLE_ADMIN) {
            throw new IllegalArgumentException("관리자만 접근할 수 있습니다.");
        }
    }
}
