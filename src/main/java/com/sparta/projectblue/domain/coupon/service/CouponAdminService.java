package com.sparta.projectblue.domain.coupon.service;

import jakarta.validation.Valid;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.projectblue.aop.annotation.CouponLogstash;
import com.sparta.projectblue.domain.common.dto.AuthUser;
import com.sparta.projectblue.domain.coupon.dto.CreateCouponRequestDto;
import com.sparta.projectblue.domain.coupon.dto.CreateCouponResponseDto;
import com.sparta.projectblue.domain.coupon.entity.Coupon;
import com.sparta.projectblue.domain.coupon.repository.CouponRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponAdminService {

    private final CouponRepository couponRepository;

    @Transactional
    @CouponLogstash
    public CreateCouponResponseDto create(
            AuthUser authUser, @Valid CreateCouponRequestDto requestDto) {

        if (requestDto.getStartDate().isAfter(requestDto.getEndDate())) {
            throw new IllegalArgumentException("쿠폰 유효일이 쿠폰 만료 이후 일 수 없습니다.");
        }

        // 쿠폰 생성
        Coupon coupon =
                new Coupon(
                        requestDto.getCouponCode(),
                        requestDto.getType(),
                        requestDto.getStatus(),
                        requestDto.getMaxQuantity(),
                        requestDto.getCurrentQuantity(),
                        requestDto.getDiscountValue(),
                        requestDto.getStartDate(),
                        requestDto.getEndDate());

        couponRepository.save(coupon);

        return new CreateCouponResponseDto(coupon);
    }

    @Transactional
    @CouponLogstash
    public void delete(AuthUser authUser, Long id) {

        Coupon coupon = couponRepository.findByIdOrElseThrow(id);

        couponRepository.delete(coupon);
    }
}
