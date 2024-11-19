package com.sparta.projectblue.domain.performance.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sparta.projectblue.config.ApiResponse;
import com.sparta.projectblue.domain.performance.dto.*;
import com.sparta.projectblue.domain.performance.service.PerformanceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/performances")
@Tag(name = "Performance", description = "공연 API")
public class PerformanceController {

    private final PerformanceService performanceService;
    private static final String NO_STORE = "no-store";

    @GetMapping
    @Operation(summary = "전체 공연리스트 조회", description = "현재 진행중인 공연 리스트 전체 출력")
    public ResponseEntity<ApiResponse<Page<GetPerformancesResponseDto>>> getPerformances(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                ApiResponse.success(performanceService.getPerformances(page, size)));
    }

    // 캐싱 적용 대상
    @GetMapping("/{id}")
    @Operation(summary = "공연 단건 조회", description = "공연 상세정보 조회")
    public ResponseEntity<ApiResponse<GetPerformanceResponseDto>> getPerformance(
            @PathVariable Long id) {

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(NO_STORE);

        return ResponseEntity.ok(ApiResponse.success(performanceService.getPerformance(id)));
    }

    // 캐싱 적용 대상
    @GetMapping("/{id}/rounds")
    @Operation(summary = "공연 회차 조회", description = "공연에 해당하는 전체 회차와 예매 가능 상태를 조회함")
    public ResponseEntity<ApiResponse<GetPerformanceRoundsResponseDto>> getRounds(
            @PathVariable Long id) {

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(NO_STORE);

        return ResponseEntity.ok(ApiResponse.success(performanceService.getRounds(id)));
    }

    // 캐싱 적용 대상
    @GetMapping("/{id}/reviews")
    @Operation(summary = "관람평 조회", description = "공연에 등록된 관람평 전체 조회")
    public ResponseEntity<ApiResponse<GetPerformanceReviewsResponseDto>> getReviews(
            @PathVariable Long id) {

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(NO_STORE);

        return ResponseEntity.ok(ApiResponse.success(performanceService.getReviews(id)));
    }

    // 캐싱 적용 대상
    @Operation(summary = "공연 출연자 조회", description = "공연 출연자 다건 조회")
    @GetMapping("/{id}/performers")
    public ResponseEntity<ApiResponse<GetPerformancePerformersResponseDto>> getPerformers(
            @PathVariable Long id) {

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(NO_STORE);

        return ResponseEntity.ok(ApiResponse.success(performanceService.getPerformers(id)));
    }
}
