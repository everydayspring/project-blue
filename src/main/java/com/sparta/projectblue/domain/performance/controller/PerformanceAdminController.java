package com.sparta.projectblue.domain.performance.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.sparta.projectblue.config.ApiResponse;
import com.sparta.projectblue.domain.common.dto.AuthUser;
import com.sparta.projectblue.domain.performance.dto.CreatePerformanceRequestDto;
import com.sparta.projectblue.domain.performance.dto.CreatePerformanceResponseDto;
import com.sparta.projectblue.domain.performance.dto.UpdatePerformanceRequestDto;
import com.sparta.projectblue.domain.performance.dto.UpdatePerformanceResponseDto;
import com.sparta.projectblue.domain.performance.service.PerformanceAdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/performances")
@Tag(name = "Admin-Performance", description = "관리자 전용 공연 API")
public class PerformanceAdminController {

    private final PerformanceAdminService performanceAdminService;

    @PostMapping
    @Operation(summary = "공연 등록", description = "파일 첨부가 필요하여 포스트맨에서 테스트할 수 있습니다...")
    public ResponseEntity<ApiResponse<CreatePerformanceResponseDto>> create(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestPart("data") CreatePerformanceRequestDto request,
            @RequestPart("file") MultipartFile posterFile) {

        return ResponseEntity.ok(
                ApiResponse.success(performanceAdminService.create(authUser, request, posterFile)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "공연 수정")
    public ResponseEntity<ApiResponse<UpdatePerformanceResponseDto>> update(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id,
            @Valid @RequestBody UpdatePerformanceRequestDto request) {

        return ResponseEntity.ok(
                ApiResponse.success(performanceAdminService.update(authUser, id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "공연 삭제")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {

        performanceAdminService.delete(authUser, id);

        return ResponseEntity.ok(ApiResponse.successWithNoContent());
    }

    @PostMapping("/{id}/performers")
    @Operation(summary = "배우 공연 등록", description = "공연에 배우를 등록합니다.")
    public ResponseEntity<ApiResponse<Void>> addPerformer(
            @PathVariable Long id, @RequestParam Long performerId) {

        performanceAdminService.addPerformer(id, performerId);

        return ResponseEntity.ok(ApiResponse.successWithNoContent());
    }

    @DeleteMapping("{id}/performers/{performerId}")
    @Operation(summary = "배우 공연 삭제", description = "공연에 등록된 배우를 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> removePerformer(
            @PathVariable Long id, @PathVariable Long performerId) {

        performanceAdminService.removePerformer(id, performerId);

        return ResponseEntity.ok(ApiResponse.successWithNoContent());
    }
}
