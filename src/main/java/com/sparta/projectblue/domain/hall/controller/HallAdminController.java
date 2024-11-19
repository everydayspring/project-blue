package com.sparta.projectblue.domain.hall.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.sparta.projectblue.config.ApiResponse;
import com.sparta.projectblue.domain.common.dto.AuthUser;
import com.sparta.projectblue.domain.hall.dto.CreateHallRequestDto;
import com.sparta.projectblue.domain.hall.dto.CreateHallResponseDto;
import com.sparta.projectblue.domain.hall.dto.UpdateHallRequestDto;
import com.sparta.projectblue.domain.hall.dto.UpdateHallResponseDto;
import com.sparta.projectblue.domain.hall.service.HallAdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/halls")
@RequiredArgsConstructor
@Tag(name = "Admin-Hall", description = "관리자 전용 공연장 API")
public class HallAdminController {

    private final HallAdminService hallAdminService;

    @PostMapping
    @Operation(summary = "공연장 등록")
    public ResponseEntity<ApiResponse<CreateHallResponseDto>> create(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody CreateHallRequestDto request) {

        return ResponseEntity.ok(ApiResponse.success(hallAdminService.create(authUser, request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "공연장 수정")
    public ResponseEntity<ApiResponse<UpdateHallResponseDto>> update(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateHallRequestDto request) {

        return ResponseEntity.ok(
                ApiResponse.success(hallAdminService.update(authUser, id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "공연장 삭제")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal AuthUser authUser, @PathVariable("id") Long id) {

        hallAdminService.delete(authUser, id);

        return ResponseEntity.ok(ApiResponse.successWithNoContent());
    }
}
