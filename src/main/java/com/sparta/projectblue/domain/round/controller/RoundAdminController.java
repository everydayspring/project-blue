package com.sparta.projectblue.domain.round.controller;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.sparta.projectblue.config.ApiResponse;
import com.sparta.projectblue.domain.common.dto.AuthUser;
import com.sparta.projectblue.domain.common.enums.PerformanceStatus;
import com.sparta.projectblue.domain.round.dto.CreateRoundRequestDto;
import com.sparta.projectblue.domain.round.dto.CreateRoundResponseDto;
import com.sparta.projectblue.domain.round.dto.UpdateRoundRequestDto;
import com.sparta.projectblue.domain.round.dto.UpdateRoundResponseDto;
import com.sparta.projectblue.domain.round.service.RoundAdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/rounds")
@RequiredArgsConstructor
@Tag(name = "Admin-Round", description = "관리자 전용 회차 API")
public class RoundAdminController {
    private final RoundAdminService roundService;

    @PostMapping
    @Operation(summary = "공연별 다건 회차 등록", description = "공연별 다건 회차를 등록합니다.")
    public ResponseEntity<ApiResponse<List<CreateRoundResponseDto>>> create(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody CreateRoundRequestDto request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        roundService.create(authUser, request.getPerformanceId(), request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "회차 수정", description = "특정 회차의 정보를 수정합니다. / 2024-12-14T18:00:00")
    public ResponseEntity<ApiResponse<UpdateRoundResponseDto>> update(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id,
            @Parameter(example = "2024-12-14T18:00:00") @RequestParam(required = false)
                    LocalDateTime date,
            @RequestParam(required = false) PerformanceStatus status) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        roundService.update(
                                authUser, id, new UpdateRoundRequestDto(date, status))));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "회차 삭제", description = "특정 회차를 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {

        roundService.delete(authUser, id);
        return ResponseEntity.ok(ApiResponse.successWithNoContent());
    }
}
