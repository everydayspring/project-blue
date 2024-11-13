package com.sparta.projectblue.domain.coupon.service;

import com.sparta.projectblue.domain.common.dto.AuthUser;
import com.sparta.projectblue.domain.common.enums.CouponStatus;
import com.sparta.projectblue.domain.common.enums.CouponType;
import com.sparta.projectblue.domain.common.enums.UserRole;
import com.sparta.projectblue.domain.coupon.entity.Coupon;
import com.sparta.projectblue.domain.coupon.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class RedissonTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    private Long couponId;

    @BeforeEach
    public void setUp() {
        // 쿠폰 생성 및 저장
        Coupon coupon =
                new Coupon(
                        "쿠폰코드",
                        CouponType.AMOUNT,
                        CouponStatus.ACTIVE,
                        5000,
                        0,
                        2000L,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(3));
        Coupon savedCoupon = couponRepository.save(coupon);
        couponId = savedCoupon.getId();
        System.out.println("쿠폰Id: " + couponId);
    }

    @Test
    public void 쿠폰발급_분산락_테스트() throws InterruptedException {
        int testCount = 1000;

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch countDownLatch = new CountDownLatch(testCount);
        AtomicInteger atomicInteger = new AtomicInteger(0);
        AtomicInteger atomicexception = new AtomicInteger(0);
        AtomicLong atomicUserId = new AtomicLong(1);

        long startTime = System.currentTimeMillis(); // 테스트 시작시간

        for (int i = 0; i < testCount; i++) {
            executorService.submit(
                    () -> {
                        try {
                            long userId = atomicUserId.getAndIncrement(); // userId 1씩 증가
                            // 각 스레드에서 고유한 AuthUser 생성
                            AuthUser authUser = new AuthUser(
                                    userId,
                                    "test" + UUID.randomUUID().toString() + "@test.com",
                                    "name" + UUID.randomUUID().toString().substring(0, 4),
                                    UserRole.ROLE_USER
                            );

                            // 쿠폰 발급 시도
                            couponService.firstCoupon(authUser, couponId);
                            atomicInteger.incrementAndGet(); // 쿠폰 발급 성공 수 증가
                        } catch (Exception e) {
                            atomicexception.incrementAndGet(); // 예외 발생 수 증가
                            System.out.println("예외발생: " + e.getMessage());
                        } finally {
                            countDownLatch.countDown(); // 작업 완료 시 카운트 감소
                        }
                    });
        }

        countDownLatch.await(); // 모든 쓰레드 작업 완료 대기
        executorService.shutdown(); // 쓰레드 풀 종료

        long endTime = System.currentTimeMillis(); // 테스트 종료 시간
        long durationInMillis = endTime - startTime; // 테스트 시간 계간
        double durationInSeconds = durationInMillis / 1000.0;

        Coupon coupon = couponRepository.findByIdOrElseThrow(couponId);

        assertEquals(
                atomicInteger.get(),
                coupon.getCurrentQuantity(),
                "성공한 발급 수량과 최종 발급 수량이 일치하지 않습니다.");
        assertTrue(
                coupon.getCurrentQuantity() <= coupon.getMaxQuantity(),
                "쿠폰이 전부 소진되었습니다."
                        + coupon.getCurrentQuantity()
                        + "/최대수량"
                        + coupon.getMaxQuantity());

        System.out.println("최종 발급된 쿠폰 수량: " + coupon.getCurrentQuantity());
        System.out.println("성공한 발급 수량: " + atomicInteger.get());
        System.out.println("락 예외 발생" + atomicexception.get());
        System.out.println("테스트 실행 시간: " + durationInSeconds + "초");
    }
}