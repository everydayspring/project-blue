package com.sparta.projectblue.domain.payment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Optional;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.sparta.projectblue.domain.common.enums.PaymentStatus;
import com.sparta.projectblue.domain.common.enums.ReservationStatus;
import com.sparta.projectblue.domain.common.enums.UserRole;
import com.sparta.projectblue.domain.common.exception.PaymentException;
import com.sparta.projectblue.domain.coupon.service.CouponService;
import com.sparta.projectblue.domain.email.service.EmailCreateService;
import com.sparta.projectblue.domain.payment.dto.PaymentResponseDto;
import com.sparta.projectblue.domain.payment.entity.Payment;
import com.sparta.projectblue.domain.payment.repository.PaymentRepository;
import com.sparta.projectblue.domain.performance.entity.Performance;
import com.sparta.projectblue.domain.performance.repository.PerformanceRepository;
import com.sparta.projectblue.domain.reservation.entity.Reservation;
import com.sparta.projectblue.domain.reservation.repository.ReservationRepository;
import com.sparta.projectblue.domain.user.entity.User;
import com.sparta.projectblue.domain.user.repository.UserRepository;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    private static final String ORDER_ID = "blueRes_20241107040730_64";
    private static final String PAYMENT_KEY = "paymentKey";
    private static final String TEST_PAYMENT_KEY = "testPaymentKey";
    private static final String ORDER_ID_VALUE_NAME = "orderId";
    private static final String AMOUNT_VALUE_NAME = "amount";
    private static final String AMOUNT = "10000";
    private static final String STATUS_VALUE_NAME = "status";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String JSON = "application/json";
    private static final String PAYMENT_CANCEL_MESSAGE = "예매 취소로 인한 결제 취소";
    private static final String PERFORMANCE_TITLE = "Performance";
    private static final String PASSWORD = "abc132?!";
    private static final String MAIL = "test@mail.com";
    private static final String TOTAL_AMOUNT = "totalAmount";

    @Mock private PaymentRepository paymentRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private PerformanceRepository performanceRepository;
    @Mock private UserRepository userRepository;
    @Mock private CouponService couponService;
    @Mock private EmailCreateService emailCreateService;

    @InjectMocks @Spy private PaymentService paymentService;

    @Mock private SavePaymentService savePaymentService;

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // 결제 서비스의 URL을 MockWebServer 주소로 변경
        ReflectionTestUtils.setField(
                paymentService, "tossBasicUrl", mockWebServer.url("/v1/payments/").toString());
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Nested
    class ConfirmPaymentTest {

        @Test
        void 결제_승인_정상_동작() throws Exception {

            // given
            String orderId = ORDER_ID;

            JSONObject requestData = new JSONObject();
            requestData.put(PAYMENT_KEY, TEST_PAYMENT_KEY);
            requestData.put(ORDER_ID_VALUE_NAME, orderId);
            requestData.put(AMOUNT_VALUE_NAME, AMOUNT);

            String jsonBody = requestData.toString();

            Payment payment = new Payment(1L, 1L, 1L, 10000L, 0L, orderId);
            ReflectionTestUtils.setField(payment, STATUS_VALUE_NAME, PaymentStatus.READY);

            given(paymentRepository.findByOrderId(anyString())).willReturn(Optional.of(payment));

            given(
                            paymentRepository.findByReservationIdAndStatus(
                                    anyLong(), any(PaymentStatus.class)))
                    .willReturn(Optional.empty());

            JSONObject responseJson = new JSONObject();
            responseJson.put("approvedAt", "2024-11-06T15:23:01Z");
            responseJson.put(ORDER_ID_VALUE_NAME, orderId);
            responseJson.put(TOTAL_AMOUNT, 10000L);

            mockWebServer.enqueue(
                    new MockResponse()
                            .setResponseCode(200)
                            .setBody(responseJson.toString())
                            .addHeader(CONTENT_TYPE, JSON));

            Reservation reservation =
                    new Reservation(1L, 1L, 1L, ReservationStatus.PENDING, 15000L);

            // when
            JSONObject response = paymentService.confirmPayment(jsonBody);

            // then
            assertEquals(orderId, response.get(ORDER_ID_VALUE_NAME));
            assertEquals(10000L, response.get(TOTAL_AMOUNT));

            assertEquals(ReservationStatus.PENDING, reservation.getStatus());
        }

        @Test
        void 결제_승인_실패_정상_동작() throws Exception {

            // given
            String orderId = ORDER_ID;

            JSONObject requestData = new JSONObject();
            requestData.put(PAYMENT_KEY, TEST_PAYMENT_KEY);
            requestData.put(ORDER_ID_VALUE_NAME, orderId);
            requestData.put(AMOUNT_VALUE_NAME, AMOUNT);

            String jsonBody = requestData.toString();

            Payment payment = new Payment(1L, 1L, 1L, 10000L, 0L, orderId);
            ReflectionTestUtils.setField(payment, STATUS_VALUE_NAME, PaymentStatus.READY);

            given(paymentRepository.findByOrderId(anyString())).willReturn(Optional.of(payment));

            given(
                            paymentRepository.findByReservationIdAndStatus(
                                    anyLong(), any(PaymentStatus.class)))
                    .willReturn(Optional.empty());

            JSONObject responseJson = new JSONObject();
            responseJson.put("approvedAt", "2024-11-06T15:23:01Z");
            responseJson.put(ORDER_ID_VALUE_NAME, orderId);
            responseJson.put(TOTAL_AMOUNT, 10000L);

            mockWebServer.enqueue(
                    new MockResponse()
                            .setResponseCode(400)
                            .setBody(responseJson.toString())
                            .addHeader(CONTENT_TYPE, JSON));

            // when
            paymentService.confirmPayment(jsonBody);

            // then
            verify(savePaymentService, never()).savePayment(any(JSONObject.class));
        }

        @Test
        void 잘못된_요청_데이터_오류() {

            // given
            String jsonBody = "{paymentKey: testPaymentKey";

            // when
            PaymentException exception =
                    assertThrows(
                            PaymentException.class, () -> paymentService.confirmPayment(jsonBody));

            // then
            assertEquals("잘못된 JSON request body", exception.getMessage());
        }

        @Test
        void 이미_결제된_예매_오류() {

            // given
            String orderId = ORDER_ID;

            JSONObject requestData = new JSONObject();
            requestData.put(PAYMENT_KEY, TEST_PAYMENT_KEY);
            requestData.put(ORDER_ID_VALUE_NAME, orderId);
            requestData.put(AMOUNT_VALUE_NAME, AMOUNT);

            String jsonBody = requestData.toString();

            Payment payment = new Payment(1L, 1L, 1L, 10000L, 0L, orderId);
            ReflectionTestUtils.setField(payment, STATUS_VALUE_NAME, PaymentStatus.DONE);

            given(paymentRepository.findByOrderId(anyString())).willReturn(Optional.of(payment));

            given(
                            paymentRepository.findByReservationIdAndStatus(
                                    anyLong(), any(PaymentStatus.class)))
                    .willReturn(Optional.of(payment));

            // when
            PaymentException exception =
                    assertThrows(
                            PaymentException.class, () -> paymentService.confirmPayment(jsonBody));

            // then
            assertEquals("이미 결제 완료된 예매정보입니다", exception.getMessage());
        }

        @Test
        void 이미_취소된_예매_오류() {

            // given
            String orderId = ORDER_ID;

            JSONObject requestData = new JSONObject();
            requestData.put(PAYMENT_KEY, TEST_PAYMENT_KEY);
            requestData.put(ORDER_ID_VALUE_NAME, orderId);
            requestData.put(AMOUNT_VALUE_NAME, AMOUNT);

            String jsonBody = requestData.toString();

            Payment payment = new Payment(1L, 1L, 1L, 10000L, 0L, orderId);
            ReflectionTestUtils.setField(payment, STATUS_VALUE_NAME, PaymentStatus.CANCELED);

            given(paymentRepository.findByOrderId(anyString())).willReturn(Optional.of(payment));

            given(paymentRepository.findByReservationIdAndStatus(anyLong(), eq(PaymentStatus.DONE)))
                    .willReturn(Optional.empty());
            given(
                            paymentRepository.findByReservationIdAndStatus(
                                    anyLong(), eq(PaymentStatus.CANCELED)))
                    .willReturn(Optional.of(payment));

            // when
            PaymentException exception =
                    assertThrows(
                            PaymentException.class, () -> paymentService.confirmPayment(jsonBody));

            // then
            assertEquals("이미 취소된 예매정보입니다", exception.getMessage());
        }

        @Test
        void 변경된_결제_가격_오류() {

            // given
            String orderId = ORDER_ID;

            JSONObject requestData = new JSONObject();
            requestData.put(PAYMENT_KEY, TEST_PAYMENT_KEY);
            requestData.put(ORDER_ID_VALUE_NAME, orderId);
            requestData.put(AMOUNT_VALUE_NAME, AMOUNT);

            String jsonBody = requestData.toString();

            Payment payment = new Payment(1L, 1L, 1L, 100000L, 0L, orderId);
            ReflectionTestUtils.setField(payment, STATUS_VALUE_NAME, PaymentStatus.READY);

            given(paymentRepository.findByOrderId(anyString())).willReturn(Optional.of(payment));

            given(
                            paymentRepository.findByReservationIdAndStatus(
                                    anyLong(), any(PaymentStatus.class)))
                    .willReturn(Optional.empty());

            // when
            PaymentException exception =
                    assertThrows(
                            PaymentException.class, () -> paymentService.confirmPayment(jsonBody));

            // then
            assertEquals("주문ID에 대한 가격이 상이합니다.", exception.getMessage());
        }
    }

    @Nested
    class CancelPaymentTest {

        @Test
        void 결제_취소_정상_동작() throws Exception {

            // given
            String paymentKey = PAYMENT_KEY;
            String cancelReason = PAYMENT_CANCEL_MESSAGE;

            Payment payment = new Payment(1L, 1L, 1L, 10000L, 0L, ORDER_ID);
            ReflectionTestUtils.setField(payment, STATUS_VALUE_NAME, PaymentStatus.READY);

            given(paymentRepository.findByPaymentKey(anyString())).willReturn(Optional.of(payment));

            mockWebServer.enqueue(
                    new MockResponse()
                            .setResponseCode(200)
                            .setBody("{}")
                            .addHeader(CONTENT_TYPE, JSON));

            // when
            String response = paymentService.cancelPayment(paymentKey, cancelReason);

            // then
            assertNotNull(response);
            assertEquals(PaymentStatus.CANCELED, payment.getStatus());
        }

        @Test
        void 결제_취소_실패_정상_동작() throws Exception {

            // given
            String paymentKey = PAYMENT_KEY;
            String cancelReason = PAYMENT_CANCEL_MESSAGE;

            Payment payment = new Payment(1L, 1L, 1L, 10000L, 0L, ORDER_ID);
            ReflectionTestUtils.setField(payment, STATUS_VALUE_NAME, PaymentStatus.READY);

            mockWebServer.enqueue(
                    new MockResponse()
                            .setResponseCode(400)
                            .setBody("{}")
                            .addHeader(CONTENT_TYPE, JSON));

            // when
            paymentService.cancelPayment(paymentKey, cancelReason);

            // then
            assertEquals(PaymentStatus.READY, payment.getStatus());
            verify(paymentRepository, never()).save(payment);
        }

        @Test
        void 결제_정보_없음_오류() {

            // given
            String paymentKey = PAYMENT_KEY;
            String cancelReason = PAYMENT_CANCEL_MESSAGE;

            given(paymentRepository.findByPaymentKey(anyString())).willReturn(Optional.empty());

            mockWebServer.enqueue(
                    new MockResponse()
                            .setResponseCode(200)
                            .setBody("{}")
                            .addHeader(CONTENT_TYPE, JSON));

            // when
            PaymentException exception =
                    assertThrows(
                            PaymentException.class,
                            () -> paymentService.cancelPayment(paymentKey, cancelReason));

            // then
            assertEquals("결제 정보를 찾을 수 없습니다.", exception.getMessage());
        }
    }

    @Nested
    class SetValueTest {
        @Test
        void 결제_정보_설정_정상_동작() {

            // given
            Long reservationId = 1L;
            Long couponId = 1L;

            Reservation reservation =
                    new Reservation(1L, 1L, 1L, ReservationStatus.PENDING, 10000L);
            ReflectionTestUtils.setField(reservation, "id", reservationId);

            given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));

            Performance performance =
                    new Performance(
                            1L,
                            PERFORMANCE_TITLE,
                            LocalDateTime.now(),
                            LocalDateTime.now().plusHours(2),
                            10000L,
                            null,
                            null,
                            500);

            given(performanceRepository.findById(anyLong())).willReturn(Optional.of(performance));

            User user = new User(MAIL, "test", PASSWORD, UserRole.ROLE_USER);

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

            given(
                            couponService.useCoupon(
                                    eq(couponId),
                                    eq(reservation.getPrice()),
                                    any(),
                                    eq(reservationId)))
                    .willReturn(2000L);

            // when
            PaymentResponseDto response = paymentService.setValue(reservationId, couponId);

            // then
            assertNotNull(response);

            assertEquals(8000L, response.getAmount() - response.getDiscountAmount());
        }

        @Test
        void 결제_정보_설정_쿠폰_없음_정상_동작() {

            // given
            Long reservationId = 1L;

            Reservation reservation =
                    new Reservation(1L, 1L, 1L, ReservationStatus.PENDING, 10000L);
            ReflectionTestUtils.setField(reservation, "id", reservationId);

            given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));

            Performance performance =
                    new Performance(
                            1L,
                            PERFORMANCE_TITLE,
                            LocalDateTime.now(),
                            LocalDateTime.now().plusHours(2),
                            10000L,
                            null,
                            null,
                            500);

            given(performanceRepository.findById(anyLong())).willReturn(Optional.of(performance));

            User user = new User(MAIL, "test", PASSWORD, UserRole.ROLE_USER);

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

            // when
            PaymentResponseDto response = paymentService.setValue(reservationId, null);

            // then
            assertNotNull(response);

            assertEquals(10000L, response.getAmount());
            assertEquals(0L, response.getDiscountAmount());
        }

        @Test
        void 결제_금액_100원_미만_정상_동작() {

            // given
            Long reservationId = 1L;
            Long couponId = 1L;

            Reservation reservation =
                    new Reservation(1L, 1L, 1L, ReservationStatus.PENDING, 10030L);
            ReflectionTestUtils.setField(reservation, "id", reservationId);

            given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));

            Performance performance =
                    new Performance(
                            1L,
                            PERFORMANCE_TITLE,
                            LocalDateTime.now(),
                            LocalDateTime.now().plusHours(2),
                            10000L,
                            null,
                            null,
                            500);

            given(performanceRepository.findById(anyLong())).willReturn(Optional.of(performance));

            User user = new User(MAIL, "test", PASSWORD, UserRole.ROLE_USER);

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

            given(
                            couponService.useCoupon(
                                    eq(couponId),
                                    eq(reservation.getPrice()),
                                    any(),
                                    eq(reservationId)))
                    .willReturn(10000L);

            // when
            PaymentResponseDto response = paymentService.setValue(reservationId, couponId);

            // then
            assertNotNull(response);

            assertEquals(100L, response.getAmount() - response.getDiscountAmount());
        }

        @Test
        void 결제_금액_마이너스_정상_동작() {

            // given
            Long reservationId = 1L;
            Long couponId = 1L;

            Reservation reservation =
                    new Reservation(1L, 1L, 1L, ReservationStatus.PENDING, 10000L);
            ReflectionTestUtils.setField(reservation, "id", reservationId);

            given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));

            Performance performance =
                    new Performance(
                            1L,
                            PERFORMANCE_TITLE,
                            LocalDateTime.now(),
                            LocalDateTime.now().plusHours(2),
                            10000L,
                            null,
                            null,
                            500);

            given(performanceRepository.findById(anyLong())).willReturn(Optional.of(performance));

            User user = new User(MAIL, "test", PASSWORD, UserRole.ROLE_USER);

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

            given(
                            couponService.useCoupon(
                                    eq(couponId),
                                    eq(reservation.getPrice()),
                                    any(),
                                    eq(reservationId)))
                    .willReturn(20000L);

            // when
            PaymentResponseDto response = paymentService.setValue(reservationId, couponId);

            // then
            assertNotNull(response);

            assertEquals(0L, response.getAmount() - response.getDiscountAmount());
        }
    }

    @Nested
    class FreePayTest {
        @Test
        void 무료_결제_정상_동작() {

            // given
            Long reservationId = 1L;
            Long couponId = 1L;

            Reservation reservation = new Reservation(1L, 1L, 1L, ReservationStatus.PENDING, 0L);
            Performance performance =
                    new Performance(
                            1L,
                            PERFORMANCE_TITLE,
                            LocalDateTime.now(),
                            LocalDateTime.now().plusHours(2),
                            10000L,
                            null,
                            null,
                            500);
            User user = new User(MAIL, "test", PASSWORD, UserRole.ROLE_USER);
            given(reservationRepository.findById(reservationId))
                    .willReturn(Optional.of(reservation));
            given(performanceRepository.findById(reservation.getPerformanceId()))
                    .willReturn(Optional.of(performance));
            given(userRepository.findById(reservation.getUserId())).willReturn(Optional.of(user));
            given(
                            couponService.useCoupon(
                                    eq(couponId),
                                    eq(reservation.getPrice()),
                                    any(),
                                    eq(reservationId)))
                    .willReturn(20000L);

            // when
            Payment payment = paymentService.freePay(reservationId, couponId);

            // then
            assertNotNull(payment);
            assertEquals(PaymentStatus.DONE, payment.getStatus());
            assertEquals(0L, payment.getOriginAmount());
        }
    }
}
