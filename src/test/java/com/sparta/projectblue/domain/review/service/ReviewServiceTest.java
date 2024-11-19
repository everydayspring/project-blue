package com.sparta.projectblue.domain.review.service;

import static com.sparta.projectblue.domain.common.enums.ReviewRate.FOUR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.sparta.projectblue.domain.common.enums.ReservationStatus;
import com.sparta.projectblue.domain.common.enums.ReviewRate;
import com.sparta.projectblue.domain.reservation.entity.Reservation;
import com.sparta.projectblue.domain.reservation.repository.ReservationRepository;
import com.sparta.projectblue.domain.review.dto.CreateReviewRequestDto;
import com.sparta.projectblue.domain.review.dto.CreateReviewResponseDto;
import com.sparta.projectblue.domain.review.dto.UpdateReviewRequestDto;
import com.sparta.projectblue.domain.review.dto.UpdateReviewResponseDto;
import com.sparta.projectblue.domain.review.entity.Review;
import com.sparta.projectblue.domain.review.repository.ReviewRepository;

@ExtendWith(SpringExtension.class)
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;

    @Mock private ReservationRepository reservationRepository;

    @InjectMocks private ReviewService reviewService;

    private Reservation reservation;
    private final Long userId = 1L;
    private final Long performanceId = 1L;
    private Long reservationId = 1L;
    private final Long reviewId = 1L;

    @BeforeEach
    void setUp() {
        reservation =
                new Reservation(userId, performanceId, 1L, ReservationStatus.COMPLETED, 10000L);
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
    }

    @Test
    void createReviewTest() {
        CreateReviewRequestDto request = new CreateReviewRequestDto(reservationId, FOUR, "별로");

        reservationId = request.getReservationId();

        // when
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reviewRepository.existsByReservationIdAndPerformanceId(reservationId, performanceId))
                .thenReturn(false);

        Review savedReview = new Review(performanceId, reservationId, ReviewRate.FOUR, "별로");
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        // when
        CreateReviewResponseDto response = reviewService.create(userId, request);

        // then
        assertNotNull(response);
        assertEquals(request.getReviewRate(), response.getReviewRate());
        assertEquals(request.getContents(), response.getContents());

        // verify: 각 메서드가 예상대로 호출되었는지 확인
        verify(reservationRepository, times(1)).findById(reservationId);
        verify(reviewRepository, times(1))
                .existsByReservationIdAndPerformanceId(reservationId, performanceId);
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void createReviewFailsWhenNoReservation() {
        CreateReviewRequestDto request =
                new CreateReviewRequestDto(reservationId, ReviewRate.FOUR, "별로");

        // when
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> reviewService.create(userId, request));
    }

    @Test
    void createReviewFailsWhenNoUser() {
        CreateReviewRequestDto request =
                new CreateReviewRequestDto(reservationId, ReviewRate.FOUR, "별로");
        reservation =
                new Reservation(
                        2L, performanceId, reservationId, ReservationStatus.COMPLETED, 10000L);
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> reviewService.create(userId, request));
    }

    @Test
    void updateReviewTest() {
        UpdateReviewRequestDto request = new UpdateReviewRequestDto(ReviewRate.FIVE, "별로인데 좀 괜찮아짐");

        Review review = new Review(performanceId, reservationId, ReviewRate.FIVE, "별로인데 좀 괜찮아짐");

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // when
        UpdateReviewResponseDto response = reviewService.update(userId, reviewId, request);

        assertNotNull(response);
        assertEquals(request.getReviewRate(), response.getReviewRate());
        assertEquals(request.getContents(), response.getContents());
        assertEquals(request.getReviewRate(), review.getReviewRate());
        assertEquals(request.getContents(), review.getContent());

        verify(reviewRepository, times(1)).findById(reviewId);
    }

    @Test
    void updateReviewFailsWhenNoUser() {
        UpdateReviewRequestDto request = new UpdateReviewRequestDto(ReviewRate.FIVE, "별로인데 좀 괜찮아짐");
        Review review = new Review(performanceId, reservationId, ReviewRate.FIVE, "별로인데 좀 괜찮아짐");
        reservation =
                new Reservation(
                        2L, performanceId, reservationId, ReservationStatus.COMPLETED, 10000L);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.update(userId, reviewId, request));
    }

    @Test
    void deleteReviewTest() {
        Review review = new Review(performanceId, reservationId, ReviewRate.FIVE, "별로인데 좀 괜찮아짐");
        reservation =
                new Reservation(
                        2L, performanceId, reservationId, ReservationStatus.COMPLETED, 10000L);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> reviewService.delete(userId, reviewId));
    }

    @Test
    void deleteReviewFailsWhenReviewNotFound() {
        // given
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> reviewService.delete(userId, reviewId));
    }

    @Test
    void deleteReviewFailsWhenReservationNotFound() {
        // given
        Review review = new Review(performanceId, reservationId, ReviewRate.FOUR, "Good");
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> reviewService.delete(userId, reviewId));
    }

    @Test
    void deleteReviewFailsWhenUserNotAuthorized() {
        // given
        Review review = new Review(performanceId, reservationId, ReviewRate.FOUR, "Good");
        reservation =
                new Reservation(
                        2L, performanceId, reservationId, ReservationStatus.COMPLETED, 10000L);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> reviewService.delete(userId, reviewId));
    }
}
