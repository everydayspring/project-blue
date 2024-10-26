package com.sparta.projectblue.domain.performance.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.sparta.projectblue.config.ApiResponse;
import com.sparta.projectblue.domain.common.dto.AuthUser;
import com.sparta.projectblue.domain.performance.dto.CreatePerformanceRequestDto;
import com.sparta.projectblue.domain.performance.dto.UpdatePerformanceRequestDto;
import com.sparta.projectblue.domain.performance.service.PerformanceAdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/performances")
@Tag(name = "PerformanceAdmin", description = "관리자 전용 공연 API")
public class PerformanceAdminController {

    private final PerformanceAdminService performanceAdminService;

    @PostMapping
    @Operation(summary = "공연 등록")
    public ResponseEntity<ApiResponse<?>> create(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestPart("data") CreatePerformanceRequestDto request,
            @RequestPart("file") MultipartFile posterFile) {

        return ResponseEntity.ok(
                ApiResponse.success(performanceAdminService.create(authUser, request, posterFile)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "공연 수정")
    public ResponseEntity<ApiResponse<?>> update(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id,
            @Valid @RequestBody UpdatePerformanceRequestDto requestDto) {

        return ResponseEntity.ok(
                ApiResponse.success(performanceAdminService.update(authUser, id, requestDto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "공연 삭제")
    public ResponseEntity<ApiResponse<?>> delete(
            @AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {

        performanceAdminService.delete(authUser, id);

        return ResponseEntity.ok(ApiResponse.successWithNoContent());
    }

    @PostMapping("/{id}/performers")
    @Operation(summary = "배우 공연 등록", description = "공연에 배우를 등록합니다.")
    public ResponseEntity<ApiResponse<?>> addPerformer(
            @PathVariable Long id, @RequestParam Long performerId) {

        performanceAdminService.addPerformer(id, performerId);

        return ResponseEntity.ok(ApiResponse.successWithNoContent());
    }

    @DeleteMapping("{id}/performers/{performerId}")
    @Operation(summary = "배우 공연 삭제", description = "공연에 등록된 배우를 삭제합니다.")
    public ResponseEntity<ApiResponse<?>> removePerformerFromPerformance(
            @PathVariable Long id, @PathVariable Long performerId) {

        performanceAdminService.removePerformer(id, performerId);

        return ResponseEntity.ok(ApiResponse.successWithNoContent());
    }
}
