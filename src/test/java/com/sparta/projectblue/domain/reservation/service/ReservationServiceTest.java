package com.sparta.projectblue.domain.reservation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.sparta.projectblue.domain.common.dto.AuthUser;
import com.sparta.projectblue.domain.common.enums.*;
import com.sparta.projectblue.domain.email.service.EmailCreateService;
import com.sparta.projectblue.domain.hall.entity.Hall;
import com.sparta.projectblue.domain.hall.repository.HallRepository;
import com.sparta.projectblue.domain.payment.entity.Payment;
import com.sparta.projectblue.domain.payment.repository.PaymentRepository;
import com.sparta.projectblue.domain.performance.entity.Performance;
import com.sparta.projectblue.domain.performance.repository.PerformanceRepository;
import com.sparta.projectblue.domain.reservation.dto.CreateReservationRequestDto;
import com.sparta.projectblue.domain.reservation.dto.CreateReservationResponseDto;
import com.sparta.projectblue.domain.reservation.dto.DeleteReservationRequestDto;
import com.sparta.projectblue.domain.reservation.dto.GetReservationResponseDto;
import com.sparta.projectblue.domain.reservation.entity.Reservation;
import com.sparta.projectblue.domain.reservation.repository.ReservationRepository;
import com.sparta.projectblue.domain.reservedseat.entity.ReservedSeat;
import com.sparta.projectblue.domain.reservedseat.repository.ReservedSeatRepository;
import com.sparta.projectblue.domain.review.repository.ReviewRepository;
import com.sparta.projectblue.domain.round.entity.Round;
import com.sparta.projectblue.domain.round.repository.RoundRepository;
import com.sparta.projectblue.domain.sse.service.NotificationService;
import com.sparta.projectblue.domain.user.entity.User;
import com.sparta.projectblue.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private static final String DESCRIPTION = "description";
    private static final String HALL_NAME = "Best Hall";
    private static final String ADDRESS = "Address 465-648";
    private static final String PERFORMANCE_TITLE = "Minions";
    private static final String PAYMENT_ID = "paymentId";
    private static final String PASSWORD = "abc132?!";
    private static final String CONCERT_TITLE_1 = "Concert 1";

    @Mock private ReservationRepository reservationRepository;

    @Mock private HallRepository hallRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private PerformanceRepository performanceRepository;
    @Mock private ReservedSeatRepository reservedSeatRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private RoundRepository roundRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmailCreateService emailCreateService;
    @Mock private NotificationService notificationService;

    @Mock private ReservationAsyncService reservationAsyncService;

    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private ReservationService reservationService;

    private final AuthUser authUser =
            new AuthUser(1L, "test@test.com", "testUser", UserRole.ROLE_USER);

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

            Performance performance =
                    new Performance(
                            1L,
                            performanceTitle,
                            LocalDateTime.now(),
                            LocalDateTime.now(),
                            price,
                            Category.CONCERT,
                            DESCRIPTION,
                            150);

            given(performanceRepository.findById(anyLong())).willReturn(Optional.of(performance));

            Hall hall = new Hall(HALL_NAME, ADDRESS, 35000);

            given(hallRepository.findById(anyLong())).willReturn(Optional.of(hall));

            // when
            CreateReservationRequestDto request = new CreateReservationRequestDto(1L, seats);

            CreateReservationResponseDto response =
                    reservationService.create(authUser.getId(), request);

            // then
            verify(reservationRepository, times(1)).save(any(Reservation.class));
            verify(reservedSeatRepository, times(seats.size())).save(any(ReservedSeat.class));

            assertEquals(ReservationStatus.PENDING, response.getStatus());
            assertEquals(seats, response.getSeats());
            assertEquals(price * seats.size(), response.getPrice());
            assertEquals(performanceTitle, response.getPerformanceTitle());
            assertEquals(roundDate, response.getRoundDate());
        }

        @Test
        void BEFORE_OPEN_상태의_회차_예매_오류() {

            // given
            Round round = new Round(1L, LocalDateTime.now(), PerformanceStatus.BEFORE_OPEN);
            given(roundRepository.findById(anyLong())).willReturn(Optional.of(round));

            Performance performance =
                    new Performance(
                            1L,
                            PERFORMANCE_TITLE,
                            LocalDateTime.now(),
                            LocalDateTime.now(),
                            156000L,
                            Category.CONCERT,
                            DESCRIPTION,
                            150);
            given(performanceRepository.findById(anyLong())).willReturn(Optional.of(performance));

            CreateReservationRequestDto request =
                    new CreateReservationRequestDto(1L, List.of(1, 2));

            Long id = authUser.getId();

            // when
            IllegalArgumentException exception =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> reservationService.create(id, request));

            // then
            assertEquals("Reservation not yet open.", exception.getMessage());
        }

        @Test
        void SOLD_OUT_상태의_회차_예매_오류() {

            // given: 회차 상태가 SOLD_OUT인 경우
            Round round = new Round(1L, LocalDateTime.now(), PerformanceStatus.SOLD_OUT);
            given(roundRepository.findById(anyLong())).willReturn(Optional.of(round));

            Performance performance =
                    new Performance(
                            1L,
                            PERFORMANCE_TITLE,
                            LocalDateTime.now(),
                            LocalDateTime.now(),
                            156000L,
                            Category.CONCERT,
                            DESCRIPTION,
                            150);
            given(performanceRepository.findById(anyLong())).willReturn(Optional.of(performance));

            CreateReservationRequestDto request =
                    new CreateReservationRequestDto(1L, List.of(1, 2));

            Long id = authUser.getId();

            // when: 예매 생성 시도
            IllegalArgumentException exception =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> reservationService.create(id, request));

            // then: 예외 메시지가 "Sold out"인지 확인
            assertEquals("Sold out", exception.getMessage());
        }

        @Test
        void 네개_이상의_좌석_예매_오류() {

            // given: 회차 상태가 AVAILABLE이며, 공연 정보와 공연장 정보가 설정된 경우
            Round round = new Round(1L, LocalDateTime.now(), PerformanceStatus.AVAILABLE);
            given(roundRepository.findById(anyLong())).willReturn(Optional.of(round));

            Performance performance =
                    new Performance(
                            1L,
                            PERFORMANCE_TITLE,
                            LocalDateTime.now(),
                            LocalDateTime.now(),
                            156000L,
                            Category.CONCERT,
                            DESCRIPTION,
                            150);
            given(performanceRepository.findById(anyLong())).willReturn(Optional.of(performance));

            Hall hall = new Hall(HALL_NAME, ADDRESS, 35000);
            given(hallRepository.findById(anyLong())).willReturn(Optional.of(hall));

            // 좌석 수가 4개를 초과하는 예약 요청을 생성
            CreateReservationRequestDto request =
                    new CreateReservationRequestDto(1L, List.of(1, 2, 5, 6, 8));

            Long id = authUser.getId();

            // when: 예매 생성 시도
            IllegalArgumentException exception =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> reservationService.create(id, request));

            // then: 예외 메시지가 "Maximum seats 4"인지 확인
            assertEquals("Maximum seats 4", exception.getMessage());
        }

        @Test
        void 공연장_좌석수_이상의_좌석번호_예매_오류() {

            // given
            Round round = new Round(1L, LocalDateTime.now(), PerformanceStatus.AVAILABLE);
            given(roundRepository.findById(anyLong())).willReturn(Optional.of(round));

            Performance performance =
                    new Performance(
                            1L,
                            PERFORMANCE_TITLE,
                            LocalDateTime.now(),
                            LocalDateTime.now(),
                            156000L,
                            Category.CONCERT,
                            DESCRIPTION,
                            150);
            given(performanceRepository.findById(anyLong())).willReturn(Optional.of(performance));

            Hall hall = new Hall(HALL_NAME, ADDRESS, 10);
            given(hallRepository.findById(anyLong())).willReturn(Optional.of(hall));

            CreateReservationRequestDto request = new CreateReservationRequestDto(1L, List.of(15));

            Long id = authUser.getId();

            // when
            IllegalArgumentException exception =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> reservationService.create(id, request));

            // then
            assertEquals("Invalid seat number", exception.getMessage());
        }

        @Test
        void 이선좌_예매_오류() {

            // given
            Round round = new Round(1L, LocalDateTime.now(), PerformanceStatus.AVAILABLE);
            given(roundRepository.findById(anyLong())).willReturn(Optional.of(round));

            Performance performance =
                    new Performance(
                            1L,
                            PERFORMANCE_TITLE,
                            LocalDateTime.now(),
                            LocalDateTime.now(),
                            156000L,
                            Category.CONCERT,
                            DESCRIPTION,
                            150);
            given(performanceRepository.findById(anyLong())).willReturn(Optional.of(performance));

            Hall hall = new Hall(HALL_NAME, ADDRESS, 150);
            given(hallRepository.findById(anyLong())).willReturn(Optional.of(hall));

            given(reservedSeatRepository.findByRoundIdAndSeatNumber(anyLong(), eq(1)))
                    .willReturn(Optional.of(new ReservedSeat(1L, round.getId(), 1)));

            CreateReservationRequestDto request =
                    new CreateReservationRequestDto(1L, List.of(1, 2));

            Long id = authUser.getId();

            // when
            IllegalArgumentException exception =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> reservationService.create(id, request));

            // then
            assertEquals("ReservedSeat already reserved", exception.getMessage());
        }
    }

    @Nested
    class DeleteReservationTest {
        @Test
        void 예매_취소_정상_동작() throws Exception {

            // given
            Reservation reservation =
                    new Reservation(authUser.getId(), 1L, 1L, ReservationStatus.COMPLETED, 15000L);
            ReflectionTestUtils.setField(reservation, "id", 1L);
            ReflectionTestUtils.setField(reservation, PAYMENT_ID, 1L);

            given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));

            User user =
                    new User(authUser.getEmail(), authUser.getName(), PASSWORD, UserRole.ROLE_USER);
            ReflectionTestUtils.setField(user, "id", authUser.getId());
            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

            given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);

            // when
            DeleteReservationRequestDto request = new DeleteReservationRequestDto(1L, PASSWORD);
            reservationService.delete(authUser.getId(), request);

            // then
            verify(reservationAsyncService, times(1)).deleteReservationSeats(anyLong());
            verify(reservationAsyncService, times(1)).deleteReservationPayment(anyLong());
            verify(reservationAsyncService, times(1))
                    .deleteReservationSlack(anyString(), anyLong());
            assertEquals(ReservationStatus.CANCELED, reservation.getStatus());
        }
    }

    @Test
    void 예매자가_아닌_경우_오류() {

        // given
        Reservation reservation =
                new Reservation(authUser.getId(), 1L, 1L, ReservationStatus.COMPLETED, 15000L);
        ReflectionTestUtils.setField(reservation, "id", 1L);
        ReflectionTestUtils.setField(reservation, PAYMENT_ID, 1L);

        given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));

        User user = new User(authUser.getEmail(), authUser.getName(), PASSWORD, UserRole.ROLE_USER);
        ReflectionTestUtils.setField(user, "id", authUser.getId() + 1L);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        // when
        DeleteReservationRequestDto request = new DeleteReservationRequestDto(1L, PASSWORD);

        Long id = authUser.getId();

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> reservationService.delete(id, request));

        // then
        assertEquals("예매자가 아닙니다", exception.getMessage());
    }

    @Test
    void 잘못된_비밀번호_오류() {

        // given
        Reservation reservation =
                new Reservation(authUser.getId(), 1L, 1L, ReservationStatus.COMPLETED, 15000L);
        ReflectionTestUtils.setField(reservation, "id", 1L);
        ReflectionTestUtils.setField(reservation, PAYMENT_ID, 1L);

        given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));

        User user =
                new User(
                        authUser.getEmail(),
                        authUser.getName(),
                        "abc132?!+++++++++++++++++",
                        UserRole.ROLE_USER);
        ReflectionTestUtils.setField(user, "id", authUser.getId());

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        // when
        DeleteReservationRequestDto request = new DeleteReservationRequestDto(1L, PASSWORD);

        Long id = authUser.getId();

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> reservationService.delete(id, request));

        // then
        assertEquals("Incorrect password", exception.getMessage());
    }

    @Nested
    class GetReservationTest {

        @Test
        void 예매_단건_조회_정상_동작() {

            // given
            Category category = Category.CONCERT;

            LocalDateTime reservationDate = LocalDateTime.now();

            Reservation reservation =
                    new Reservation(authUser.getId(), 1L, 1L, ReservationStatus.COMPLETED, 15000L);
            ReflectionTestUtils.setField(reservation, "id", 1L);
            ReflectionTestUtils.setField(reservation, PAYMENT_ID, 1L);
            ReflectionTestUtils.setField(reservation, "createdAt", reservationDate);

            given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));

            Performance performance =
                    new Performance(
                            1L,
                            CONCERT_TITLE_1,
                            LocalDateTime.now(),
                            LocalDateTime.now(),
                            15000L,
                            category,
                            DESCRIPTION,
                            150);
            given(performanceRepository.findById(anyLong())).willReturn(Optional.of(performance));

            Round round = new Round(1L, LocalDateTime.now(), PerformanceStatus.AVAILABLE);
            given(roundRepository.findById(anyLong())).willReturn(Optional.of(round));

            List<ReservedSeat> reservedSeats =
                    List.of(new ReservedSeat(1L, 1L, 1), new ReservedSeat(1L, 1L, 2));
            given(reservedSeatRepository.findByReservationId(anyLong())).willReturn(reservedSeats);

            Payment payment =
                    new Payment(authUser.getId(), reservation.getId(), 1L, 15000L, 0L, "orderId");
            ReflectionTestUtils.setField(payment, "status", PaymentStatus.DONE);
            given(paymentRepository.findById(anyLong())).willReturn(Optional.of(payment));

            // when
            GetReservationResponseDto response =
                    reservationService.getReservation(authUser, reservation.getId());

            // then
            assertEquals(category, response.getCategory());
            assertEquals(performance.getTitle(), response.getPerformanceTitle());
            assertEquals(round.getDate(), response.getRound());
            assertEquals(reservationDate, response.getReservationDate());
            assertEquals(reservation.getId(), response.getReservationId());
            assertEquals(List.of(1, 2), response.getSeats());
            assertEquals(ReservationStatus.COMPLETED, response.getReservationStatus());
        }

        @Test
        void 예매자가_아님_오류() {

            // given
            Reservation reservation =
                    new Reservation(
                            authUser.getId() + 1, 1L, 1L, ReservationStatus.COMPLETED, 15000L);
            ReflectionTestUtils.setField(reservation, "id", 1L);

            given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));

            Long id = reservation.getId();

            // when
            IllegalArgumentException exception =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> reservationService.getReservation(authUser, id));

            // then
            assertEquals("예매자가 아닙니다", exception.getMessage());
        }

        @Test
        void 좌석정보_없음_오류() {

            // given
            Reservation reservation =
                    new Reservation(authUser.getId(), 1L, 1L, ReservationStatus.COMPLETED, 15000L);
            ReflectionTestUtils.setField(reservation, "id", 1L);

            given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));

            Performance performance =
                    new Performance(
                            1L,
                            CONCERT_TITLE_1,
                            LocalDateTime.now(),
                            LocalDateTime.now(),
                            15000L,
                            Category.CONCERT,
                            DESCRIPTION,
                            150);
            given(performanceRepository.findById(anyLong())).willReturn(Optional.of(performance));

            Round round = new Round(1L, LocalDateTime.now(), PerformanceStatus.AVAILABLE);
            given(roundRepository.findById(anyLong())).willReturn(Optional.of(round));

            given(reservedSeatRepository.findByReservationId(anyLong())).willReturn(List.of());

            Long id = reservation.getId();

            // when
            IllegalArgumentException exception =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> reservationService.getReservation(authUser, id));

            // then
            assertEquals("ReservedSeat does not exist", exception.getMessage());
        }

        @Test
        void 결제정보_없음_오류() {

            // given
            Reservation reservation =
                    new Reservation(authUser.getId(), 1L, 1L, ReservationStatus.COMPLETED, 15000L);
            ReflectionTestUtils.setField(reservation, "id", 1L);
            ReflectionTestUtils.setField(reservation, PAYMENT_ID, 1L);

            given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));

            Performance performance =
                    new Performance(
                            1L,
                            CONCERT_TITLE_1,
                            LocalDateTime.now(),
                            LocalDateTime.now(),
                            15000L,
                            Category.CONCERT,
                            DESCRIPTION,
                            150);
            given(performanceRepository.findById(anyLong())).willReturn(Optional.of(performance));

            Round round = new Round(1L, LocalDateTime.now(), PerformanceStatus.AVAILABLE);
            given(roundRepository.findById(anyLong())).willReturn(Optional.of(round));

            List<ReservedSeat> reservedSeats =
                    List.of(new ReservedSeat(1L, 1L, 1), new ReservedSeat(1L, 1L, 2));
            given(reservedSeatRepository.findByReservationId(anyLong())).willReturn(reservedSeats);

            given(paymentRepository.findById(anyLong())).willReturn(Optional.empty());

            Long id = reservation.getId();

            // when
            IllegalArgumentException exception =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> reservationService.getReservation(authUser, id));

            // then
            assertEquals("결제 정보를 찾을 수 없습니다", exception.getMessage());
        }
    }
}
