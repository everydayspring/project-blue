package com.sparta.projectblue.domain.coupon.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.sparta.projectblue.config.ApiResponse;
import com.sparta.projectblue.domain.common.dto.AuthUser;
import com.sparta.projectblue.domain.coupon.entity.Coupon;
import com.sparta.projectblue.domain.coupon.service.CouponService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coupons")
@Tag(name = "Coupon", description = "쿠폰 API")
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/first/{couponId}")
    @Operation(summary = "쿠폰 발행")
    public ResponseEntity<ApiResponse<Coupon>> firstCoupon(
            @AuthenticationPrincipal AuthUser authUser, @PathVariable Long couponId) {

        return ResponseEntity.ok(
                ApiResponse.success(couponService.firstCoupon(authUser, couponId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "쿠폰 단건 조회")
    public ResponseEntity<ApiResponse<?>> getCoupon(@PathVariable("id") Long id) {

        return ResponseEntity.ok(ApiResponse.success(couponService.getCoupon(id)));
    }

    @GetMapping
    @Operation(summary = "쿠폰 다건 조회")
    public ResponseEntity<ApiResponse<?>> getUserCoupons(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                ApiResponse.success(couponService.getUserCoupon(authUser, page, size)));
    }
}
