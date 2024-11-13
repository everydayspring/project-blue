//package com.sparta.projectblue;
//
//import com.sparta.projectblue.domain.reservation.dto.CreateReservationRequestDto;
//import com.sparta.projectblue.domain.reservation.service.ReservationService;
//import com.sparta.projectblue.domain.round.repository.RoundRepository;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Order;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.List;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//// @ExtendWith(MockitoExtension.class)
////@MockitoSettings(strictness = Strictness.LENIENT)
//@SpringBootTest
//public class ReservationConcurrencyTest {
//
//    @Autowired
//    private ReservationService reservationService;
//
//    private final Long userId = 1L;
//    private Long roundId = 2L;
//    private String reservationKey;
//    private int seatNumber=3;
//
//    @Autowired
//    private RoundRepository roundRepository;
//
//    @BeforeEach
//    void reservationSetting() {
//
//        this.reservationKey = "reservation_lock_" + roundId;
//    }
//
//
//    // 락을 사용한 80명의 동시 예매 시도
//    @Test
//    @Order(1)
//    void lockUse() throws InterruptedException {
//        final int numberOfMember = 2; // 80
//        final CountDownLatch countDownLatch = new CountDownLatch(numberOfMember);
//
//        AtomicInteger successCount = new AtomicInteger(0);
//        List<Thread> threadList = Stream
//                .generate(() -> new Thread(new UsingLockMember(this.reservationKey, countDownLatch, successCount)))
//                .limit(numberOfMember)
//                .collect(Collectors.toList());
//
//        threadList.forEach(Thread::start);
//        countDownLatch.await();
//
//        Assertions.assertEquals(numberOfMember, successCount.get(), "Only one reservation should succeed with lock");
//    }
//
//    // 락 없이 100명의 동시 예매 시도
//    @Test
//    @Order(2)
//    void lockNotUse() throws InterruptedException {
//        final int numberOfMember = 2;
//        final CountDownLatch countDownLatch = new CountDownLatch(numberOfMember);
//
//        AtomicInteger successCount = new AtomicInteger(0);
//        List<Thread> threadList = Stream
//                .generate(() -> new Thread(new NoLockMember(this.reservationKey, countDownLatch, successCount)))
//                .limit(numberOfMember)
//                .collect(Collectors.toList());
//
//        threadList.forEach(Thread::start);
//        countDownLatch.await();
//
//        Assertions.assertTrue(successCount.get() > 1, "Multiple reservations should succeed without lock");
//    }
//
//    private class UsingLockMember implements Runnable {
//        private final String reservationKey;
//        private final CountDownLatch countDownLatch;
//        private final AtomicInteger successCount;
//
//        public UsingLockMember(String reservationKey, CountDownLatch countDownLatch, AtomicInteger successCount) {
//            this.reservationKey = reservationKey;
//            this.countDownLatch = countDownLatch;
//            this.successCount = successCount;
//        }
//
//        @Override
//        public void run() {
//            seatNumber++;
//            CreateReservationRequestDto requestDto = new CreateReservationRequestDto(roundId, List.of(seatNumber));
//            try {
//                reservationService.create(userId, requestDto);
//                successCount.incrementAndGet();
//            } catch (IllegalStateException e) {
//              throw new IllegalArgumentException("실패");
//            } finally {
//                countDownLatch.countDown();
//            }
//        }
//    }
//
//
//    private class NoLockMember implements Runnable {
//        private final String reservationKey;
//        private final CountDownLatch countDownLatch;
//        private final AtomicInteger successCount;
//
//        public NoLockMember(String reservationKey, CountDownLatch countDownLatch, AtomicInteger successCount) {
//            this.reservationKey = reservationKey;
//            this.countDownLatch = countDownLatch;
//            this.successCount = successCount;
//        }
//
//        @Override
//        public void run() {
//            CreateReservationRequestDto requestDto = new CreateReservationRequestDto(roundId, List.of(1, 2));
//            try {
//                reservationService.create(userId, requestDto);
//                successCount.incrementAndGet();
//            } catch (IllegalStateException e) {
//                throw new IllegalArgumentException("Reservation already exists");
//            } finally {
//                countDownLatch.countDown();
//            }
//        }
//    }
//}
