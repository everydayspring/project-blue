package com.sparta.projectblue.domain.performance.controller;

import com.sparta.projectblue.config.ApiResponse;
import com.sparta.projectblue.domain.common.dto.AuthUser;
import com.sparta.projectblue.domain.performance.dto.PerformanceRequestDto;
import com.sparta.projectblue.domain.performance.service.PerformanceAdminService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/performances")
public class PerformanceAdminController {

    private final PerformanceAdminService performanceAdminService;

    @PostMapping
    @Operation(summary = "공연 등록")
    public ResponseEntity<ApiResponse<String>> create(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody PerformanceRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.success(performanceAdminService.create(authUser, requestDto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "공연 삭제")
    public ResponseEntity<ApiResponse<String>> delete(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(performanceAdminService.delete(authUser, id)));
    }
}