package com.sparta.projectblue.domain.coupon.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.projectblue.aop.annotation.CouponLogstash;
import com.sparta.projectblue.config.DistributedLock;
import com.sparta.projectblue.domain.common.dto.AuthUser;
import com.sparta.projectblue.domain.common.enums.CouponStatus;
import com.sparta.projectblue.domain.common.enums.CouponType;
import com.sparta.projectblue.domain.coupon.dto.GetCouponResponseDto;
import com.sparta.projectblue.domain.coupon.entity.Coupon;
import com.sparta.projectblue.domain.coupon.repository.CouponRepository;
import com.sparta.projectblue.domain.usedcoupon.entity.UsedCoupon;
import com.sparta.projectblue.domain.usedcoupon.repository.UsedCouponRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final UsedCouponRepository usedCouponRepository;

    @DistributedLock(key = "#couponId")
    @Transactional
    public Coupon firstCoupon(AuthUser authUser, Long couponId) {

        Coupon coupon = couponRepository.findByIdOrElseThrow(couponId);

        if (usedCouponRepository.existsByCouponIdAndUserId(couponId, authUser.getId())) {
            throw new IllegalArgumentException("이미 발급받은 쿠폰입니다.");
        }
        if (coupon.getCurrentQuantity() >= coupon.getMaxQuantity()) {
            throw new IllegalArgumentException("쿠폰이 모두 소진되었습니다.");
        }

        // 발급된 쿠폰 저장
        saveUsedCoupon(couponId, authUser);

        // 현재 쿠폰 수량 증가
        coupon.incrementQuantity();

        return couponRepository.save(coupon);
    }

    public GetCouponResponseDto getCoupon(Long id) {

        Coupon coupon = couponRepository.findByIdOrElseThrow(id);

        return new GetCouponResponseDto(coupon);
    }

    public Page<GetCouponResponseDto> getUserCoupon(int page, int size) {

        Pageable pageable = PageRequest.of(page - 1, size);

        return couponRepository.findAll(pageable).map(GetCouponResponseDto::new);
    }

    @Transactional
    @CouponLogstash
    public Long useCoupon(Long id, Long originPrice, Long userId, Long reservationId) {

        Coupon coupon = couponRepository.findByIdOrElseThrow(id);

        if (coupon.getStatus().equals(CouponStatus.USED)) {
            throw new IllegalArgumentException("이미 사용된 쿠폰 입니다.");
        }
        if (coupon.getStatus().equals(CouponStatus.EXPIRED)) {
            throw new IllegalArgumentException("기간 만료된 쿠폰 입니다.");
        }

        if (usedCouponRepository.existsByCouponIdAndUserId(coupon.getId(), userId)) {
            throw new IllegalArgumentException("이미 사용한 쿠폰 입니다.");
        }

        // 타입별 할인 금액 계산
        Long discountAmount;
        if (coupon.getType() == CouponType.RATE) {
            discountAmount = Math.round(originPrice * (coupon.getDiscountValue() / 100.0));
        } else {
            discountAmount = coupon.getDiscountValue(); // CouponType.AMOUNT
        }

        // UsedCoupon DB 저장
        UsedCoupon usedCoupon =
                new UsedCoupon(
                        coupon.getId(),
                        userId,
                        reservationId,
                        LocalDateTime.now(),
                        discountAmount,
                        LocalDateTime.now());
        usedCouponRepository.save(usedCoupon);

        return discountAmount;
    }

    public void saveUsedCoupon(Long couponId, AuthUser authUser) {
        // 발급된 쿠폰 기록 저장
        new UsedCoupon(
                couponId,
                authUser.getId(),
                null,
                LocalDateTime.now(),
                getCoupon(couponId).getDiscountValue(),
                LocalDateTime.now());
    }
}
