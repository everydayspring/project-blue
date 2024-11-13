package com.sparta.projectblue.domain.performance.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sparta.projectblue.domain.common.enums.ReviewRate;

import lombok.Getter;

@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class GetPerformanceReviewsResponseDto {

    private List<ReviewInfo> reviews;

    public GetPerformanceReviewsResponseDto() {}

    public GetPerformanceReviewsResponseDto(List<ReviewInfo> reviews) {

        this.reviews = reviews;
    }

    @Getter
    public static class ReviewInfo {

        private ReviewRate rate;
        private String content;

        public ReviewInfo() {}

        public ReviewInfo(ReviewRate rate, String content) {

            this.rate = rate;
            this.content = content;
        }
    }
}
