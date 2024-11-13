package com.sparta.projectblue.domain.reservation.service;

import com.sparta.projectblue.domain.common.dto.AuthUser;
import com.sparta.projectblue.domain.common.enums.*;
import com.sparta.projectblue.domain.hall.entity.Hall;
import com.sparta.projectblue.domain.hall.repository.HallRepository;
import com.sparta.projectblue.domain.payment.entity.Payment;
import com.sparta.projectblue.domain.payment.repository.PaymentRepository;
import com.sparta.projectblue.domain.payment.service.PaymentService;
import com.sparta.projectblue.domain.performance.entity.Performance;
import com.sparta.projectblue.domain.performance.repository.PerformanceRepository;
import com.sparta.projectblue.domain.reservation.dto.*;
import com.sparta.projectblue.domain.reservation.entity.Reservation;
import com.sparta.projectblue.domain.reservation.repository.ReservationRepository;
import com.sparta.projectblue.domain.reservedSeat.entity.ReservedSeat;
import com.sparta.projectblue.domain.reservedSeat.repository.ReservedSeatRepository;
import com.sparta.projectblue.domain.review.repository.ReviewRepository;
import com.sparta.projectblue.domain.round.entity.Round;
import com.sparta.projectblue.domain.round.repository.RoundRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private HallRepository hallRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PerformanceRepository performanceRepository;
    @Mock
    private ReservedSeatRepository reservedSeatRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private RoundRepository roundRepository;
    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ReservationService reservationService;

    private final AuthUser authUser = new AuthUser(1L, "test@test.com", "testUser", UserRole.ROLE_USER);

    @Nested
    class CreateReservationTest {

        @Test
        void 예매_정상_동작() {

            // given
            List<Integer> seats = List.of(1, 2);

            String performanceTitle = "KuromiZZang";

            Long price = 50000L;

            LocalDateTime roundDate = LocalDateTime.now();

            Round round = new Round(1L, roundDate, PerformanceStatus.AVAILABLE);

            given(roundRepository.findById(anyLong())).willReturn(Optional.of(round));

            Performance performance = new Performance(1L, performanceTitle, LocalDateTime.now(), LocalDateTime.now(), price, Category.CONCERT, "description", 150);

            given(performanceRepository.findById(anyLong())).willReturn(Optional.of(performance));

            Hall hall = new Hall("Best Hall", "Address 465-648", 35000);

            given(hallRepository.findById(anyLong())).willReturn(Optional.of(hall));

            // when
            CreateReservationRequestDto request = new CreateReservationRequestDto(1L, seats);

            CreateReservationResponseDto response = reservationService.create(authUser.getId(), request);

            // then
            verify(reservationRepository, times(1)).save(any(Reservation.class));
            verify(reservedSeatRepository, times(seats.size())).save(any(ReservedSeat.class));

            assertEquals(response.getStatus(), ReservationStatus.PENDING);
            assertEquals(response.getSeats(), seats);
            assertEquals(response.getPrice(), price * seats.size());
            assertEquals(response.getPerformanceTitle(), performanceTitle);
            assertEquals(response.getRoundDate(), roundDate);
        }

        @Test
        void BEFORE_OPEN_상태의_회차_예매_오류() {

            // given
            Round round = new Round(1L, LocalDateTime.now(), PerformanceStatus.BEFORE_OPEN);

            given(roundRepository.findById(anyLong())).willReturn(Optional.of(round));

            Performance performance = new Performance(1L, "Minions", LocalDateTime.now(), LocalDateTime.now(), 156000L, Category.CONCERT, "description", 150);

            given(performanceRepository.findById(anyLong())).willReturn(Optional.of(performance));

            // when
            CreateReservationRequestDto request = new CreateReservationRequestDto(1L, List.of(1, 2));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    reservationService.create(authUser.getId(), request));

            // then
            assertEquals(exception.getMessage(), "Reservation not yet open.");
        }

        @Test
        void SOLD_OUT_상태의_회차_예매_오류() {

            // given
            Round round = new Round(1L, LocalDateTime.now(), PerformanceStatus.SOLD_OUT);

            given(roundRepository.findById(anyLong())).willReturn(Optional.of(round));

            Performance performance = new Performance(1L, "Minions", LocalDateTime.now(), LocalDateTime.now(), 156000L, Category.CONCERT, "description", 150);

            given(performanceRepository.findById(anyLong())).willReturn(Optional.of(performance));

            // when
            CreateReservationRequestDto request = new CreateReservationRequestDto(1L, List.of(1, 2));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    reservationService.create(authUser.getId(), request));

            // then
            assertEquals(exception.getMessage(), "Sold out");
        }

        @Test
        void 네개_이상의_좌석_예매_오류() {

            // given
            Round round = new Round(1L, LocalDateTime.now(), PerformanceStatus.AVAILABLE);

            given(roundRepository.findById(anyLong())).willReturn(Optional.of(round));

            Performance performance = new Performance(1L, "Minions", LocalDateTime.now(), LocalDateTime.now(), 156000L, Category.CONCERT, "description", 150);

            given(performanceRepository.findById(anyLong())).willReturn(Optional.of(performance));

            Hall hall = new Hall("Best Hall", "Address 465-648", 35000);

            given(hallRepository.findById(anyLong())).willReturn(Optional.of(hall));

            // when
            CreateReservationRequestDto request = new CreateReservationRequestDto(1L, List.of(1, 2, 5, 6, 8));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    reservationService.create(authUser.getId(), request));

            // then
            assertEquals(exception.getMessage(), "Maximum seats 4");
        }

        @Test
        void 공연장_좌석수_이상의_좌석번호_예매_오류() {

            // given
            Round round = new Round(1L, LocalDateTime.now(), PerformanceStatus.AVAILABLE);

            given(roundRepository.findById(anyLong())).willReturn(Optional.of(round));

            Performance performance = new Performance(1L, "Minions", LocalDateTime.now(), LocalDateTime.now(), 156000L, Category.CONCERT, "description", 150);

            given(performanceRepository.findById(anyLong())).willReturn(Optional.of(performance));

            Hall hall = new Hall("Best Hall", "Address 465-648", 10);

            given(hallRepository.findById(anyLong())).willReturn(Optional.of(hall));

            // when
            CreateReservationRequestDto request = new CreateReservationRequestDto(1L, List.of(15));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    reservationService.create(authUser.getId(), request));

            // then
            assertEquals(exception.getMessage(), "Invalid seat number");
        }

        @Test
        void 이선좌_예매_오류() {

            // given
            Round round = new Round(1L, LocalDateTime.now(), PerformanceStatus.AVAILABLE);

            given(roundRepository.findById(anyLong())).willReturn(Optional.of(round));

            Performance performance = new Performance(1L, "Minions", LocalDateTime.now(), LocalDateTime.now(), 156000L, Category.CONCERT, "description", 150);

            given(performanceRepository.findById(anyLong())).willReturn(Optional.of(performance));

            Hall hall = new Hall("Best Hall", "Address 465-648", 150);

            given(hallRepository.findById(anyLong())).willReturn(Optional.of(hall));

            given(reservedSeatRepository.findByRoundIdAndSeatNumber(anyLong(), eq(1)))
                    .willReturn(Optional.of(new ReservedSeat(1L, round.getId(), 1)));

            // when
            CreateReservationRequestDto request = new CreateReservationRequestDto(1L, List.of(1, 2));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    reservationService.create(authUser.getId(), request));

            // then
            assertEquals(exception.getMessage(), "ReservedSeat already reserved");
        }
    }

    @Nested
    class DeleteReservationTest {
        @Test
        void 예매_취소_정상_동작() throws Exception {

            // given
            Reservation reservation = new Reservation(authUser.getId(), 1L, 1L, ReservationStatus.COMPLETED, 15000L);

            ReflectionTestUtils.setField(reservation, "id", 1L);
            ReflectionTestUtils.setField(reservation, "paymentId", 1L);

            given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));

            User user = new User(authUser.getEmail(), authUser.getName(), "abc132?!", UserRole.ROLE_USER);

            ReflectionTestUtils.setField(user, "id", authUser.getId());

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

            given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);

            ReservedSeat reservedSeat1 = new ReservedSeat(1L, 1L, 1);
            ReservedSeat reservedSeat2 = new ReservedSeat(1L, 1L, 2);
            ReservedSeat reservedSeat3 = new ReservedSeat(1L, 1L, 3);

            List<ReservedSeat> reservedSeats = List.of(reservedSeat1, reservedSeat2, reservedSeat3);

            given(reservedSeatRepository.findByReservationId(any())).willReturn(reservedSeats);

            Payment payment = new Payment(authUser.getId(), reservation.getId(), 1L, 15000L, 0L, "orderId");

            ReflectionTestUtils.setField(payment, "status", PaymentStatus.DONE);

            given(paymentRepository.findById(any())).willReturn(Optional.of(payment));

            // when
            DeleteReservationRequestDto request = new DeleteReservationRequestDto(1L, "abc132?!");

            reservationService.delete(authUser.getId(), request);

            // then
            verify(reservedSeatRepository, times(1)).deleteAll(anyList());

            assertEquals(reservation.getStatus(), ReservationStatus.CANCELED);
        }

        @Test
        void 결제_정보가_없는_예매_취소_정상_동작() throws Exception {

            // given
            Reservation reservation = new Reservation(authUser.getId(), 1L, 1L, ReservationStatus.COMPLETED, 15000L);

            ReflectionTestUtils.setField(reservation, "id", 1L);
            ReflectionTestUtils.setField(reservation, "paymentId", null);

            given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));

            User user = new User(authUser.getEmail(), authUser.getName(), "abc132?!", UserRole.ROLE_USER);

            ReflectionTestUtils.setField(user, "id", authUser.getId());

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

            given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);

            ReservedSeat reservedSeat1 = new ReservedSeat(1L, 1L, 1);
            ReservedSeat reservedSeat2 = new ReservedSeat(1L, 1L, 2);
            ReservedSeat reservedSeat3 = new ReservedSeat(1L, 1L, 3);

            List<ReservedSeat> reservedSeats = List.of(reservedSeat1, reservedSeat2, reservedSeat3);

            given(reservedSeatRepository.findByReservationId(any())).willReturn(reservedSeats);


            // when
            DeleteReservationRequestDto request = new DeleteReservationRequestDto(1L, "abc132?!");

            reservationService.delete(authUser.getId(), request);

            // then
            verify(reservedSeatRepository, times(1)).deleteAll(anyList());

            assertEquals(reservation.getStatus(), ReservationStatus.CANCELED);
        }

        @Test
        void 예매자가_아닌_경우_오류() throws Exception {

            // given
            Reservation reservation = new Reservation(authUser.getId(), 1L, 1L, ReservationStatus.COMPLETED, 15000L);

            ReflectionTestUtils.setField(reservation, "id", 1L);
            ReflectionTestUtils.setField(reservation, "paymentId", 1L);

            given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));

            User user = new User(authUser.getEmail(), authUser.getName(), "abc132?!", UserRole.ROLE_USER);

            ReflectionTestUtils.setField(user, "id", authUser.getId() + 1L);

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

            // when
            DeleteReservationRequestDto request = new DeleteReservationRequestDto(1L, "abc132?!");

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    reservationService.delete(authUser.getId(), request));

            // then
            assertEquals(exception.getMessage(), "예매자가 아닙니다");
        }

        @Test
        void 잘못된_비밀번호_오류() throws Exception {

            // given
            Reservation reservation = new Reservation(authUser.getId(), 1L, 1L, ReservationStatus.COMPLETED, 15000L);

            ReflectionTestUtils.setField(reservation, "id", 1L);
            ReflectionTestUtils.setField(reservation, "paymentId", 1L);

            given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));

            User user = new User(authUser.getEmail(), authUser.getName(), "abc132?!+++++++++++++++++", UserRole.ROLE_USER);

            ReflectionTestUtils.setField(user, "id", authUser.getId());

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));


            // when
            DeleteReservationRequestDto request = new DeleteReservationRequestDto(1L, "abc132?!");

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    reservationService.delete(authUser.getId(), request));

            // then
            assertEquals(exception.getMessage(), "Incorrect password");
        }

        @Test
        void 이미_취소된_예매_오류() throws Exception {

            // given
            Reservation reservation = new Reservation(authUser.getId(), 1L, 1L, ReservationStatus.CANCELED, 15000L);

            ReflectionTestUtils.setField(reservation, "id", 1L);
            ReflectionTestUtils.setField(reservation, "paymentId", 1L);

            given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));

            User user = new User(authUser.getEmail(), authUser.getName(), "abc132?!", UserRole.ROLE_USER);

            ReflectionTestUtils.setField(user, "id", authUser.getId());

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

            given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);

            // when
            DeleteReservationRequestDto request = new DeleteReservationRequestDto(1L, "abc132?!");

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    reservationService.delete(authUser.getId(), request));

            // then
            assertEquals(exception.getMessage(), "Reservation already cancelled.");
        }

        @Test
        void 예매_좌석정보가_없음_오류() throws Exception {

            // given
            Reservation reservation = new Reservation(authUser.getId(), 1L, 1L, ReservationStatus.COMPLETED, 15000L);

            ReflectionTestUtils.setField(reservation, "id", 1L);
            ReflectionTestUtils.setField(reservation, "paymentId", 1L);

            given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));

            User user = new User(authUser.getEmail(), authUser.getName(), "abc132?!", UserRole.ROLE_USER);

            ReflectionTestUtils.setField(user, "id", authUser.getId());

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

            given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);

            List<ReservedSeat> reservedSeats = List.of();

            given(reservedSeatRepository.findByReservationId(any())).willReturn(reservedSeats);

            // when
            DeleteReservationRequestDto request = new DeleteReservationRequestDto(1L, "abc132?!");

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    reservationService.delete(authUser.getId(), request));

            // then
            assertEquals(exception.getMessage(), "ReservedSeat does not exist");
        }
    }

    @Nested
    class GetReservationTest {

        @Test
        void 예매_단건_조회_정상_동작() {

            // given
            Category category = Category.CONCERT;

            LocalDateTime reservationDate = LocalDateTime.now();

            Reservation reservation = new Reservation(authUser.getId(), 1L, 1L, ReservationStatus.COMPLETED, 15000L);
            ReflectionTestUtils.setField(reservation, "id", 1L);
            ReflectionTestUtils.setField(reservation, "paymentId", 1L);
            ReflectionTestUtils.setField(reservation, "createdAt", reservationDate);

            given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));

            Performance performance = new Performance(1L, "Concert", LocalDateTime.now(), LocalDateTime.now(), 15000L, category, "description", 150);
            given(performanceRepository.findById(anyLong())).willReturn(Optional.of(performance));

            Round round = new Round(1L, LocalDateTime.now(), PerformanceStatus.AVAILABLE);
            given(roundRepository.findById(anyLong())).willReturn(Optional.of(round));

            List<ReservedSeat> reservedSeats = List.of(new ReservedSeat(1L, 1L, 1), new ReservedSeat(1L, 1L, 2));
            given(reservedSeatRepository.findByReservationId(anyLong())).willReturn(reservedSeats);

            Payment payment = new Payment(authUser.getId(), reservation.getId(), 1L, 15000L, 0L, "orderId");
            ReflectionTestUtils.setField(payment, "status", PaymentStatus.DONE);
            given(paymentRepository.findById(anyLong())).willReturn(Optional.of(payment));

            // when
            GetReservationResponseDto response = reservationService.getReservation(authUser, reservation.getId());

            // then
            assertEquals(response.getCategory(), category);
            assertEquals(response.getPerformanceTitle(), performance.getTitle());
            assertEquals(response.getRound(), round.getDate());
            assertEquals(response.getReservationDate(), reservationDate);
            assertEquals(response.getReservationId(), reservation.getId());
            assertEquals(response.getSeats(), List.of(1, 2));
            assertEquals(response.getReservationStatus(), ReservationStatus.COMPLETED);
        }

        @Test
        void 예매자가_아님_오류() {

            // given
            Reservation reservation = new Reservation(authUser.getId() + 1, 1L, 1L, ReservationStatus.COMPLETED, 15000L);
            ReflectionTestUtils.setField(reservation, "id", 1L);

            given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));

            // when
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    reservationService.getReservation(authUser, reservation.getId()));

            // then
            assertEquals(exception.getMessage(), "예매자가 아닙니다");
        }

        @Test
        void 좌석정보_없음_오류() {

            // given
            Reservation reservation = new Reservation(authUser.getId(), 1L, 1L, ReservationStatus.COMPLETED, 15000L);
            ReflectionTestUtils.setField(reservation, "id", 1L);

            given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));

            Performance performance = new Performance(1L, "Concert", LocalDateTime.now(), LocalDateTime.now(), 15000L, Category.CONCERT, "description", 150);
            given(performanceRepository.findById(anyLong())).willReturn(Optional.of(performance));

            Round round = new Round(1L, LocalDateTime.now(), PerformanceStatus.AVAILABLE);
            given(roundRepository.findById(anyLong())).willReturn(Optional.of(round));

            given(reservedSeatRepository.findByReservationId(anyLong())).willReturn(List.of());

            // when
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    reservationService.getReservation(authUser, reservation.getId()));

            // then
            assertEquals(exception.getMessage(), "ReservedSeat does not exist");
        }

        @Test
        void 결제정보_없음_오류() {

            // given
            Reservation reservation = new Reservation(authUser.getId(), 1L, 1L, ReservationStatus.COMPLETED, 15000L);
            ReflectionTestUtils.setField(reservation, "id", 1L);
            ReflectionTestUtils.setField(reservation, "paymentId", 1L);

            given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));

            Performance performance = new Performance(1L, "Concert", LocalDateTime.now(), LocalDateTime.now(), 15000L, Category.CONCERT, "description", 150);
            given(performanceRepository.findById(anyLong())).willReturn(Optional.of(performance));

            Round round = new Round(1L, LocalDateTime.now(), PerformanceStatus.AVAILABLE);
            given(roundRepository.findById(anyLong())).willReturn(Optional.of(round));

            List<ReservedSeat> reservedSeats = List.of(new ReservedSeat(1L, 1L, 1), new ReservedSeat(1L, 1L, 2));
            given(reservedSeatRepository.findByReservationId(anyLong())).willReturn(reservedSeats);

            given(paymentRepository.findById(anyLong())).willReturn(Optional.empty());

            // when
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    reservationService.getReservation(authUser, reservation.getId()));

            // then
            assertEquals(exception.getMessage(), "결제 정보를 찾을 수 없습니다");
        }
    }

    @Nested
    class GetReservationsTest {

        @Test
        void 예매_다건_조회_정상_동작() {

            // given
            Long userId = 1L;

            Reservation reservation1 = new Reservation(userId, 1L, 1L, ReservationStatus.COMPLETED, 15000L);
            ReflectionTestUtils.setField(reservation1, "id", 1L);
            Reservation reservation2 = new Reservation(userId, 2L, 2L, ReservationStatus.CANCELED, 20000L);
            ReflectionTestUtils.setField(reservation2, "id", 2L);

            given(reservationRepository.findByUserId(userId)).willReturn(List.of(reservation1, reservation2));

            Performance performance1 = new Performance(1L, "Concert1", LocalDateTime.now(), LocalDateTime.now(), 15000L, Category.CONCERT, "description", 150);
            Performance performance2 = new Performance(2L, "Concert2", LocalDateTime.now(), LocalDateTime.now(), 20000L, Category.CONCERT, "description", 200);

            given(performanceRepository.findById(reservation1.getPerformanceId())).willReturn(Optional.of(performance1));
            given(performanceRepository.findById(reservation2.getPerformanceId())).willReturn(Optional.of(performance2));

            List<ReservedSeat> seats1 = List.of(new ReservedSeat(1L, reservation1.getId(), 1), new ReservedSeat(2L, reservation1.getId(), 2));
            List<ReservedSeat> seats2 = List.of(new ReservedSeat(3L, reservation2.getId(), 1));

            given(reservedSeatRepository.findByReservationId(reservation1.getId())).willReturn(seats1);
            given(reservedSeatRepository.findByReservationId(reservation2.getId())).willReturn(seats2);

            Hall hall1 = new Hall("Main Hall", "Address 123", 300);
            Hall hall2 = new Hall("Side Hall", "Address 456", 150);

            given(hallRepository.findById(performance1.getHallId())).willReturn(Optional.of(hall1));
            given(hallRepository.findById(performance2.getHallId())).willReturn(Optional.of(hall2));

            Round round1 = new Round(1L, LocalDateTime.now().plusDays(1), PerformanceStatus.AVAILABLE);
            Round round2 = new Round(2L, LocalDateTime.now().plusDays(2), PerformanceStatus.SOLD_OUT);

            given(roundRepository.findById(reservation1.getRoundId())).willReturn(Optional.of(round1));
            given(roundRepository.findById(reservation2.getRoundId())).willReturn(Optional.of(round2));

            // when
            List<GetReservationsResponseDto> responseList = reservationService.getReservations(userId);

            // then
            assertEquals(2, responseList.size());

            GetReservationsResponseDto response1 = responseList.get(0);
            assertEquals("Concert1", response1.getPerformanceTitle());
            assertEquals(2, response1.getTickets());
            assertEquals(reservation1.getId(), response1.getReservationId());
            assertEquals(hall1.getName(), response1.getHallName());
            assertEquals(round1.getDate(), response1.getRound());
            assertEquals(reservation1.getStatus(), response1.getStatus());

            GetReservationsResponseDto response2 = responseList.get(1);
            assertEquals("Concert2", response2.getPerformanceTitle());
            assertEquals(1, response2.getTickets());
            assertEquals(reservation2.getId(), response2.getReservationId());
            assertEquals(hall2.getName(), response2.getHallName());
            assertEquals(round2.getDate(), response2.getRound());
            assertEquals(reservation2.getStatus(), response2.getStatus());
        }

        @Test
        void 공연_없음_오류() {

            // given
            Long userId = 1L;
            Reservation reservation = new Reservation(userId, 1L, 1L, ReservationStatus.COMPLETED, 15000L);

            given(reservationRepository.findByUserId(userId)).willReturn(List.of(reservation));

            given(performanceRepository.findById(reservation.getPerformanceId())).willReturn(Optional.empty());

            // when
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    reservationService.getReservations(userId));

            // then
            assertEquals("Performance not found for reservation", exception.getMessage());
        }

        @Test
        void 공연장_없음_오류() {

            // given
            Long userId = 1L;
            Reservation reservation = new Reservation(userId, 1L, 1L, ReservationStatus.COMPLETED, 15000L);

            given(reservationRepository.findByUserId(userId)).willReturn(List.of(reservation));

            Performance performance = new Performance(1L, "Concert1", LocalDateTime.now(), LocalDateTime.now(), 15000L, Category.CONCERT, "description", 150);
            given(performanceRepository.findById(reservation.getPerformanceId())).willReturn(Optional.of(performance));

            given(hallRepository.findById(performance.getHallId())).willReturn(Optional.empty());

            // when
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    reservationService.getReservations(userId));

            // then
            assertEquals("공연장을 찾을 수 없습니다", exception.getMessage());
        }

        @Test
        void 회차_없음_오류() {

            // given
            Long userId = 1L;
            Reservation reservation = new Reservation(userId, 1L, 1L, ReservationStatus.COMPLETED, 15000L);

            given(reservationRepository.findByUserId(userId)).willReturn(List.of(reservation));

            Performance performance = new Performance(1L, "Concert1", LocalDateTime.now(), LocalDateTime.now(), 15000L, Category.CONCERT, "description", 150);
            given(performanceRepository.findById(reservation.getPerformanceId())).willReturn(Optional.of(performance));

            Hall hall = new Hall("Main Hall", "Address 123", 300);
            given(hallRepository.findById(performance.getHallId())).willReturn(Optional.of(hall));

            given(roundRepository.findById(reservation.getRoundId())).willReturn(Optional.empty());

            // when
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    reservationService.getReservations(userId));

            // then
            assertEquals("공연 회차를 찾을 수 없습니다", exception.getMessage());
        }
    }
}
