package com.sparta.projectblue.domain.reservation.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sparta.projectblue.domain.reservation.entity.Reservation;
import com.sparta.projectblue.domain.search.dto.UserBookingDto;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Page<Reservation> findByUserId(Long userId, Pageable pageable);

    @Query(
            """
        SELECT new com.sparta.projectblue.domain.search.dto.UserBookingDto(
            r.id, u.name, u.id, p.title, r.createdAt, pay.amountTotal,
            r.status, pay.id, pay.approvedAt)
        FROM Reservation r
        JOIN User u ON r.userId = u.id
        JOIN Performance p ON r.performanceId = p.id
        LEFT JOIN Payment pay ON r.paymentId = pay.id
    """)
    List<UserBookingDto> findUserBookingData();
}
