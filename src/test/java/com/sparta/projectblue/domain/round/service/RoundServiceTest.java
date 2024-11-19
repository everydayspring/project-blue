package com.sparta.projectblue.domain.round.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.sparta.projectblue.domain.common.enums.Category;
import com.sparta.projectblue.domain.common.enums.PerformanceStatus;
import com.sparta.projectblue.domain.hall.entity.Hall;
import com.sparta.projectblue.domain.hall.repository.HallRepository;
import com.sparta.projectblue.domain.performance.entity.Performance;
import com.sparta.projectblue.domain.performance.repository.PerformanceRepository;
import com.sparta.projectblue.domain.reservedseat.entity.ReservedSeat;
import com.sparta.projectblue.domain.reservedseat.repository.ReservedSeatRepository;
import com.sparta.projectblue.domain.round.dto.GetRoundAvailableSeatsResponseDto;
import com.sparta.projectblue.domain.round.entity.Round;
import com.sparta.projectblue.domain.round.repository.RoundRepository;

@ExtendWith(MockitoExtension.class)
class RoundServiceTest {

    @Mock private RoundRepository roundRepository;

    @Mock private HallRepository hallRepository;

    @Mock private PerformanceRepository performanceRepository;

    @Mock private ReservedSeatRepository reservedSeatRepository;

    @InjectMocks private RoundService roundService;

    @Test
    void 사용_가능한_좌석_목록을_반환한다() {
        // given
        Long roundId = 1L;
        Round round = new Round(1L, LocalDateTime.now(), PerformanceStatus.AVAILABLE);
        ReflectionTestUtils.setField(round, "id", 1L);

        Performance performance =
                new Performance(
                        1L,
                        "Test Performance",
                        LocalDateTime.of(2024, 11, 6, 10, 0),
                        LocalDateTime.of(2024, 11, 30, 22, 0),
                        50000L,
                        Category.MUSICAL,
                        "Test musical performance",
                        120);
        ReflectionTestUtils.setField(performance, "id", 1L);

        Hall hall = new Hall("Test Hall", "123 Test St", 10);
        ReflectionTestUtils.setField(hall, "id", 1L);

        // 예매된 자석: 3번 5번
        List<ReservedSeat> reservedSeats =
                List.of(new ReservedSeat(1L, 1L, 3), new ReservedSeat(2L, 1L, 5));

        when(roundRepository.findById(roundId)).thenReturn(Optional.of(round));
        when(performanceRepository.findById(anyLong())).thenReturn(Optional.of(performance));
        when(hallRepository.findById(anyLong())).thenReturn(Optional.of(hall));
        when(reservedSeatRepository.findByRoundId(roundId)).thenReturn(reservedSeats);

        // when
        GetRoundAvailableSeatsResponseDto result = roundService.getAvailableSeats(roundId);

        // then
        assertNotNull(result);
        assertEquals("Test Performance", result.getPerformanceTitle());
        assertEquals(round.getDate(), result.getRoundDate());

        List<Integer> expectedAvailableSeats = List.of(1, 2, 4, 6, 7, 8, 9, 10);
        assertEquals(expectedAvailableSeats, result.getSeats());
    }

    @Test
    void 존재하지_않는_회차를_조회할_때_예외를_던진다() {
        // given
        Long roundId = 1L;
        when(roundRepository.findById(roundId)).thenReturn(Optional.empty());

        // then
        Exception exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            roundService.getAvailableSeats(roundId);
                        });

        assertEquals("회차를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void BEFORE_OPEN_회차를_조회할_때_예외를_던진다() {
        // given
        Long roundId = 1L;
        Round round = new Round(1L, LocalDateTime.now(), PerformanceStatus.BEFORE_OPEN);
        when(roundRepository.findById(roundId)).thenReturn(Optional.of(round));

        // when & then
        Exception exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            roundService.getAvailableSeats(roundId);
                        });

        assertEquals("예약이 아직 시작되지 않았습니다.", exception.getMessage());
    }

    @Test
    void SOLD_OUT_회차를_조회할_때_예외를_던진다() {
        // given
        Long roundId = 1L;
        Round round = new Round(1L, LocalDateTime.now(), PerformanceStatus.SOLD_OUT);
        when(roundRepository.findById(roundId)).thenReturn(Optional.of(round));

        // when & then
        Exception exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            roundService.getAvailableSeats(roundId);
                        });

        assertEquals("이미 매진되었습니다.", exception.getMessage());
    }

    @Test
    void 회차에_해당하는_공연이_존재하지_않을_때_예외를_던진다() {
        // given
        Long roundId = 1L;
        Round round = new Round(1L, LocalDateTime.now(), PerformanceStatus.AVAILABLE);
        when(roundRepository.findById(roundId)).thenReturn(Optional.of(round));
        when(performanceRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        Exception exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            roundService.getAvailableSeats(roundId);
                        });

        assertEquals("공연을 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void 회차에_해당하는_공연장이_존재하지_않을_때_예외를_던진다() {
        // given
        Long roundId = 1L;
        Round round = new Round(1L, LocalDateTime.now(), PerformanceStatus.AVAILABLE);
        ReflectionTestUtils.setField(round, "id", 1L);

        Performance performance =
                new Performance(
                        1L,
                        "Test Performance",
                        LocalDateTime.of(2024, 11, 6, 10, 0),
                        LocalDateTime.of(2024, 11, 30, 22, 0),
                        50000L,
                        Category.MUSICAL,
                        "Test musical performance",
                        120);
        ReflectionTestUtils.setField(performance, "id", 1L);

        when(roundRepository.findById(roundId)).thenReturn(Optional.of(round));
        when(performanceRepository.findById(anyLong())).thenReturn(Optional.of(performance));
        when(hallRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            roundService.getAvailableSeats(roundId);
                        });

        assertEquals("공연장을 찾을 수 없습니다.", exception.getMessage());
    }
}
