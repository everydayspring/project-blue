package com.sparta.projectblue.domain.coupon.repository;

import com.sparta.projectblue.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    default Coupon findByIdOrElseThrow(Long id) {
        return findById(id).orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));
    }

}
