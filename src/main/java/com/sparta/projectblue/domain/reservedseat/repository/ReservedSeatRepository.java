package com.sparta.projectblue.domain.reservedseat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.projectblue.domain.reservedseat.entity.ReservedSeat;

public interface ReservedSeatRepository extends JpaRepository<ReservedSeat, Long> {

    Optional<ReservedSeat> findByRoundIdAndSeatNumber(Long roundId, Integer seatNumber);

    List<ReservedSeat> findByReservationId(Long reservationId);

    List<ReservedSeat> findByRoundId(Long roundId);
}
