package com.sparta.projectblue.domain.coupon.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.sparta.projectblue.config.ApiResponse;
import com.sparta.projectblue.domain.common.dto.AuthUser;
import com.sparta.projectblue.domain.coupon.dto.CreateCouponRequestDto;
import com.sparta.projectblue.domain.coupon.dto.CreateCouponResponseDto;
import com.sparta.projectblue.domain.coupon.service.CouponAdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/coupons")
@Tag(name = "Admin-Coupon", description = "쿠폰 관리자 API")
public class CouponAdminController {

    private final CouponAdminService couponAdminService;

    @PostMapping
    @Operation(summary = "쿠폰 등록")
    public ResponseEntity<ApiResponse<CreateCouponResponseDto>> create(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody CreateCouponRequestDto requestDto) {

        return ResponseEntity.ok(
                ApiResponse.success(couponAdminService.create(authUser, requestDto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "쿠폰 삭제")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal AuthUser authUser, @Valid @PathVariable("id") Long id) {

        couponAdminService.delete(authUser, id);

        return ResponseEntity.ok(ApiResponse.successWithNoContent());
    }
}
