package com.sparta.projectblue.domain.review.dto;

import com.sparta.projectblue.domain.common.enums.ReviewRate;
import com.sparta.projectblue.domain.review.entity.Review;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CreateReviewDto {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {

        @NotNull
        private Long reservationId;

        @NotNull
        private ReviewRate reviewRate;

        @NotNull
        private String contents;
    }

    @Getter
    public static class Response {
        private final Long id;
        private final Long performanceId;
        private final Long reservationId;
        private final ReviewRate reviewRate;
        private final String contents;

        public Response(Review review) {
            this.id = review.getId();
            this.performanceId = review.getPerformanceId();
            this.reservationId = review.getReservationId();
            this.reviewRate = review.getReviewRate();
            this.contents = review.getContent();
        }
    }

}

