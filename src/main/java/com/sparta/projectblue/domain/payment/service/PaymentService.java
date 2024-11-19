package com.sparta.projectblue.domain.payment.service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Objects;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.projectblue.aop.annotation.PaymentLogstash;
import com.sparta.projectblue.domain.common.enums.PaymentStatus;
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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final PerformanceRepository performanceRepository;
    private final UserRepository userRepository;
    private final CouponService couponService;
    private final EmailCreateService emailCreateService;
    private final SavePaymentService savePaymentService;

    @Value("${toss.basic.url}")
    private String tossBasicUrl;

    @Value("${toss.widget.secret.key}")
    private String widgetSecretKey;

    @Transactional
    public JSONObject confirmPayment(String jsonBody)
            throws PaymentException, IOException, ParseException {

        JSONParser parser = new JSONParser();
        String orderId;
        String amount;
        String paymentKey;
        try {
            // 클라이언트에서 받은 JSON 요청 바디
            JSONObject requestData = (JSONObject) parser.parse(jsonBody);
            paymentKey = (String) requestData.get("paymentKey");
            orderId = (String) requestData.get("orderId");
            amount = (String) requestData.get("amount");
        } catch (ParseException e) {
            throw new PaymentException("잘못된 JSON request body");
        }

        if (!verifyPayment(orderId, Long.parseLong(amount))) {
            throw new PaymentException("주문ID에 대한 가격이 상이합니다.");
        }

        JSONObject obj = new JSONObject();
        obj.put("orderId", orderId);
        obj.put("amount", amount);
        obj.put("paymentKey", paymentKey);

        String authorizations = secretKeyEncoder();

        // 결제 승인 API 호출
        // @docs https://docs.tosspayments.com/guides/v2/payment-widget/integration#3-결제-승인하기
        URL url = new URL(tossBasicUrl + "confirm");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", authorizations);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(obj.toString().getBytes("UTF-8"));

        int code = connection.getResponseCode();
        boolean isSuccess = code == 200;

        InputStream responseStream =
                isSuccess ? connection.getInputStream() : connection.getErrorStream();

        Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8);
        JSONObject jsonObject = (JSONObject) parser.parse(reader);
        responseStream.close();

        if (isSuccess) {
            // 결제 승인 후 처리
            savePaymentService.savePayment(jsonObject);
        }

        return jsonObject;
    }

    @Transactional
    @PaymentLogstash
    public String cancelPayment(String paymentKey, String cancelReason)
            throws PaymentException, IOException {

        // 취소 API 호출
        URL url = new URL(tossBasicUrl + paymentKey + "/cancel");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", secretKeyEncoder());
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        // 취소 요청 데이터 설정
        JSONObject obj = new JSONObject();
        obj.put("cancelReason", cancelReason);

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(obj.toString().getBytes("UTF-8"));

        int code = connection.getResponseCode();
        boolean isSuccess = code == 200;

        if (isSuccess) {
            // payment 상태 취소 변경
            Payment payment =
                    paymentRepository
                            .findByPaymentKey(paymentKey)
                            .orElseThrow(() -> new PaymentException("결제 정보를 찾을 수 없습니다."));

            payment.canceled();
        }

        return String.valueOf(obj);
    }

    @Transactional
    public PaymentResponseDto setValue(Long reservationId, Long couponId) {

        Reservation reservation =
                reservationRepository
                        .findById(reservationId)
                        .orElseThrow(() -> new IllegalArgumentException("예약 내역이 없습니다."));

        Performance performance =
                performanceRepository
                        .findById(reservation.getPerformanceId())
                        .orElseThrow(() -> new IllegalArgumentException("해당 공연이 존재하지 않습니다."));

        User user =
                userRepository
                        .findById(reservation.getUserId())
                        .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        Long originPrice = reservation.getPrice();

        Long discountValue = 0L;
        if (couponId != null) {
            discountValue =
                    couponService.useCoupon(couponId, originPrice, user.getId(), reservationId);
        }

        long userPay = originPrice - discountValue;

        if (userPay > 0 && userPay < 100) {
            originPrice += (100L - userPay);
        } else if (userPay < 0) {
            discountValue = originPrice;
        }

        String timestamp =
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        String orderId = "blueRes_" + timestamp + "_" + reservationId;

        Payment payment =
                new Payment(
                        user.getId(),
                        reservation.getId(),
                        performance.getId(),
                        originPrice,
                        discountValue,
                        orderId);

        paymentRepository.save(payment);

        return new PaymentResponseDto(
                orderId,
                performance.getTitle(),
                originPrice,
                user.getEmail(),
                user.getName(),
                discountValue);
    }

    @Transactional
    @PaymentLogstash
    public Payment freePay(Long reservationId, Long couponId) {
        Reservation reservation =
                reservationRepository
                        .findById(reservationId)
                        .orElseThrow(() -> new IllegalArgumentException("예약 내역이 없습니다."));

        Performance performance =
                performanceRepository
                        .findById(reservation.getPerformanceId())
                        .orElseThrow(() -> new IllegalArgumentException("해당 공연이 존재하지 않습니다."));

        User user =
                userRepository
                        .findById(reservation.getUserId())
                        .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        String timestamp =
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        String orderId = "blueRes_" + timestamp + "_" + reservationId;

        Long originPrice = reservation.getPrice();
        Long discountValue =
                couponService.useCoupon(couponId, originPrice, user.getId(), reservationId);

        Payment payment =
                new Payment(
                        user.getId(),
                        reservation.getId(),
                        performance.getId(),
                        originPrice,
                        discountValue,
                        orderId);

        paymentRepository.save(payment);

        payment.addPaymentInfo(null, null, null, null, null, LocalDateTime.now(), 0L);
        reservation.addPaymentId(payment.getId());
        reservation.resCompleted();

        emailCreateService.sendPaymentEmail(user.getId(), payment);

        return payment;
    }

    public String secretKeyEncoder() {

        // 토스페이먼츠 API는 시크릿 키를 사용자 ID로 사용하고, 비밀번호는 사용하지 않습니다.
        // 비밀번호가 없다는 것을 알리기 위해 시크릿 키 뒤에 콜론을 추가합니다.
        // @docs https://docs.tosspayments.com/reference/using-api/authorization#%EC%9D%B8%EC%A6%9D
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodedBytes =
                encoder.encode((widgetSecretKey + ":").getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedBytes);
    }

    private boolean verifyPayment(String orderId, Long amount) {

        Payment payment =
                paymentRepository
                        .findByOrderId(orderId)
                        .orElseThrow(() -> new PaymentException("결제 내역이 존재하지 않습니다."));

        Long reservationId = Long.parseLong(orderId.substring(23));

        if (paymentRepository
                .findByReservationIdAndStatus(reservationId, PaymentStatus.DONE)
                .isPresent()) {
            throw new PaymentException("이미 결제 완료된 예매정보입니다");
        }

        if (paymentRepository
                .findByReservationIdAndStatus(reservationId, PaymentStatus.CANCELED)
                .isPresent()) {
            throw new PaymentException("이미 취소된 예매정보입니다");
        }

        return Objects.equals(amount, payment.getOriginAmount() - payment.getDiscountValue());
    }
}
