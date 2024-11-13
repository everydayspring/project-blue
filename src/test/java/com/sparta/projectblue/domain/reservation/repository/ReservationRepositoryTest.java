package com.sparta.projectblue.domain.reservation.repository;

import com.sparta.projectblue.config.QueryDslConfig;
import com.sparta.projectblue.domain.common.enums.ReservationStatus;
import com.sparta.projectblue.domain.reservation.entity.Reservation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReservationRepositoryTest {

    private final ReservationRepository reservationRepository;

    @Autowired
    ReservationRepositoryTest(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Test
    void 저장() {

        // given
        Random random = new Random();
        long userId = 1 + (long) (random.nextInt(100));
        long performanceId = 1 + (long) (random.nextInt(100));
        long roundId = 1 + (long) (random.nextInt(100));

        Reservation reservation = new Reservation(userId, performanceId, roundId, ReservationStatus.PENDING, 10000L);

        // when
        reservationRepository.save(reservation);

        // then
        assertEquals(reservation.getUserId(), userId);
        assertEquals(reservation.getPerformanceId(), performanceId);
        assertEquals(reservation.getRoundId(), roundId);
        assertEquals(reservation.getStatus(), ReservationStatus.PENDING);
        assertEquals(reservation.getPrice(), 10000L);
    }
}
