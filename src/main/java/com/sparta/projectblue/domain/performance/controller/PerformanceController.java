package com.sparta.projectblue.domain.performance.controller;


import com.sparta.projectblue.config.ApiResponse;
import com.sparta.projectblue.domain.performance.dto.PerformanceDetailDto;
import com.sparta.projectblue.domain.performance.dto.PerformanceResponseDto;
import com.sparta.projectblue.domain.performance.service.PerformanceService;
import com.sparta.projectblue.domain.performer.dto.PerformerDetailDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/performances")
@Tag(name = "Performance", description = "공연 API")
public class PerformanceController {

    private final PerformanceService performanceService;

    // 전체 조회
    @GetMapping
    @Operation(summary = "전체 공연리스트 조회", description = "현재 진행중인 공연 리스트 전체 출력")
    public ResponseEntity<ApiResponse<Page<PerformanceResponseDto>>> getPerformances(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(performanceService.getPerformances(page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "공연 단건 조회", description = "공연 상세정보 조회")
    public ResponseEntity<ApiResponse<PerformanceDetailDto>> getPerformance(
            @PathVariable Long id) {
        PerformanceDetailDto dto = performanceService.getPerformance(id);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @Operation(summary = "공연 출연자 조회", description = "공연 출연자 다건 조회")
    @GetMapping("/{id}/performers")
    public ResponseEntity<ApiResponse<List<PerformerDetailDto>>> getPerformers(
            @PathVariable Long id) {
        List<PerformerDetailDto> performers = performanceService.getPerformers(id);
        return ResponseEntity.ok(ApiResponse.success(performers));
    }

    @GetMapping("/{id}/rounds")
    @Operation(summary = "공연 회차 조회", description = "공연에 해당하는 전체 회차와 예매 가능 상태를 조회함")
    public ResponseEntity<ApiResponse<?>> getRounds(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(performanceService.getRounds(id)));
    }
  
    @GetMapping("/{id}/reviews")
    @Operation(summary = "관람평 조회", description = "공연에 등록된 관람평 전체 조회")
    public ResponseEntity<ApiResponse<?>> getReviews(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(performanceService.getReviews(id)));
    }

    @PostMapping("/{id}/performers")
    @Operation(summary = "배우 공연 등록", description = "공연에 배우를 등록합니다.")
    public ResponseEntity<ApiResponse<?>> addPerformer(
            @PathVariable Long id,
            @RequestParam Long performerId) {
        performanceService.addPerformer(id, performerId);
        return ResponseEntity.ok(ApiResponse.successWithNoContent());
    }

    @DeleteMapping("{id}/performers/{performerId}")
    @Operation(summary = "배우 공연 삭제", description = "공연에 등록된 배우를 삭제합니다.")
    public ResponseEntity<ApiResponse<?>> removePerformerFromPerformance(
            @PathVariable Long id,
            @PathVariable Long performerId) {
        performanceService.removePerformer(id, performerId);
        return ResponseEntity.ok(ApiResponse.successWithNoContent());
    }
}
