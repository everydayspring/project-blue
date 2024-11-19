package com.sparta.projectblue.domain.review.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.sparta.projectblue.config.ApiResponse;
import com.sparta.projectblue.domain.common.dto.AuthUser;
import com.sparta.projectblue.domain.review.dto.CreateReviewRequestDto;
import com.sparta.projectblue.domain.review.dto.CreateReviewResponseDto;
import com.sparta.projectblue.domain.review.dto.UpdateReviewRequestDto;
import com.sparta.projectblue.domain.review.dto.UpdateReviewResponseDto;
import com.sparta.projectblue.domain.review.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "관람평 관련 API")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "관람평 작성", description = "예매한 공연에 관람평 작성 가능")
    public ResponseEntity<ApiResponse<CreateReviewResponseDto>> create(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody CreateReviewRequestDto request) {

        return ResponseEntity.ok(
                ApiResponse.success(reviewService.create(authUser.getId(), request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "관람평 수정")
    public ResponseEntity<ApiResponse<UpdateReviewResponseDto>> update(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id,
            @RequestBody UpdateReviewRequestDto request) {

        return ResponseEntity.ok(
                ApiResponse.success(reviewService.update(authUser.getId(), id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "관람평 삭제")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {

        reviewService.delete(authUser.getId(), id);

        return ResponseEntity.ok(ApiResponse.successWithNoContent());
    }
}
