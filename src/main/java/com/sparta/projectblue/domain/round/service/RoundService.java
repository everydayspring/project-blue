package com.sparta.projectblue.domain.round.service;

import com.sparta.projectblue.domain.common.enums.PerformanceStatus;
import com.sparta.projectblue.domain.hall.entity.Hall;
import com.sparta.projectblue.domain.hall.repository.HallRepository;
import com.sparta.projectblue.domain.performance.entity.Performance;
import com.sparta.projectblue.domain.performance.repository.PerformanceRepository;
import com.sparta.projectblue.domain.reservedSeat.entity.ReservedSeat;
import com.sparta.projectblue.domain.reservedSeat.repository.ReservedSeatRepository;
import com.sparta.projectblue.domain.round.dto.*;
import com.sparta.projectblue.domain.round.entity.Round;
import com.sparta.projectblue.domain.round.repository.RoundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoundService {

    private final RoundRepository roundRepository;

    private final HallRepository hallRepository;
    private final PerformanceRepository performanceRepository;
    private final ReservedSeatRepository reservedSeatRepository;

    public GetRoundAvailableSeatsResponseDto getAvailableSeats(Long id) {
        // 회차 가져옴
        Round round = roundRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("회차를 찾을 수 없습니다."));

        // 오픈전
        if (round.getStatus().equals(PerformanceStatus.BEFORE_OPEN)) {
            throw new IllegalArgumentException("예약이 아직 시작되지 않았습니다.");
        }

        // 매진
        if (round.getStatus().equals(PerformanceStatus.SOLD_OUT)) {
            throw new IllegalArgumentException("이미 매진되었습니다.");
        }

        // 공연 가져옴
        Performance performance = performanceRepository.findById(round.getPerformanceId()).orElseThrow(() ->
                new IllegalArgumentException("출연자를 찾을 수 없습니다."));

        // 공연장 가져옴
        Hall hall = hallRepository.findById(performance.getHallId()).orElseThrow(() ->
                new IllegalArgumentException("공연장을 찾을 수 없습니다."));


        // 예약된 좌석 가져옴
        List<ReservedSeat> reservedSeats = reservedSeatRepository.findByRoundId(round.getId());
        Set<Integer> reservedSeatNumbers = reservedSeats.stream()
                .map(ReservedSeat::getSeatNumber)
                .collect(Collectors.toSet());

        // 전체 좌석수 가져옴
        int totalSeats = hall.getSeats();
        List<Integer> availableSeats = new ArrayList<>();

        // 예약된 자석 제외하고 리스트에 넣음
        for (int seatNumber = 1; seatNumber <= totalSeats; seatNumber++) {
            if (!reservedSeatNumbers.contains(seatNumber)) {
                availableSeats.add(seatNumber);
            }
        }

        return new GetRoundAvailableSeatsResponseDto(performance.getTitle(), round.getDate(), availableSeats);
    }

    @Transactional
    public List<CreateRoundResponseDto> createRounds(Long id, CreateRoundRequestDto request) {
        // 공연 존재 여부 확인
        if (performanceRepository.findById(id).isEmpty()) {
            throw new IllegalArgumentException("공연을 찾을 수 없습니다");
        }
        LocalDateTime now = LocalDateTime.now();

        List<Round> newRounds = request.getDates().stream()
                .peek(date -> {
                    if (date.isBefore(now)) {
                        throw new IllegalArgumentException("과거의 날짜로 회차를 생성할 수 없습니다.");
                    }
                })
                .map(date -> new Round(id, date, PerformanceStatus.BEFORE_OPEN))
                .collect(Collectors.toList());

        List<Round> savedRounds = roundRepository.saveAll(newRounds);

        return savedRounds.stream()
                .map(round -> new CreateRoundResponseDto(round.getId(), round.getPerformanceId(), round.getDate(), round.getStatus()))
                .collect(Collectors.toList());
    }

    @Transactional
    public UpdateRoundResponseDto updateRound(Long id, UpdateRoundRequestDto request) {
        // 회차 가져옴
        Round round = roundRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("회차를 찾을 수 없습니다."));

        LocalDateTime now = LocalDateTime.now();

        if (request.getDate().isBefore(now)) {
            throw new IllegalArgumentException("과거의 날짜로 회차를 수정할 수 없습니다.");
        }

        round.updateDate(request.getDate());
        round.updateStatus(request.getStatus());
        roundRepository.save(round);

        return new UpdateRoundResponseDto(round.getId(), round.getPerformanceId(), round.getDate(), round.getStatus());
    }

    @Transactional
    public void deleteRound(Long id) {
        Round round = roundRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("회차를 찾을 수 없습니다."));

        roundRepository.delete(round);
    }




}
