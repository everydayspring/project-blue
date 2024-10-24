package com.sparta.projectblue.domain.reservedSeat.repository;

import com.sparta.projectblue.domain.reservedSeat.entity.ReservedSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservedSeatRepository extends JpaRepository<ReservedSeat, Long> {
    Optional<ReservedSeat> findByRoundIdAndSeatNumber(Long roundId, Integer seatNumber);

    List<ReservedSeat> findByReservationId(Long reservationId);

    List<ReservedSeat> findByRoundId(Long roundId);
}
