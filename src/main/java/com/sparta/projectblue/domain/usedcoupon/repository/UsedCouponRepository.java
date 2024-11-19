package com.sparta.projectblue.domain.usedcoupon.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.projectblue.domain.usedcoupon.entity.UsedCoupon;

public interface UsedCouponRepository extends JpaRepository<UsedCoupon, Long> {
    boolean existsByCouponIdAndUserId(Long couponId, Long userId);
}
