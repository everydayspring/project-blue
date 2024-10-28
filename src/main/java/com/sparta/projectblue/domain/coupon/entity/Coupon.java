package com.sparta.projectblue.domain.coupon.entity;

import com.sparta.projectblue.domain.common.entity.BaseEntity;
import com.sparta.projectblue.domain.common.enums.CouponStatus;
import com.sparta.projectblue.domain.common.enums.CouponType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "Coupon")
public class Coupon extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String couponCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CouponType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CouponStatus status;

    @Column(nullable = false)
    private int maxQuantity;

    @Column(nullable = false)
    private int currentQuantity;

    @Column(nullable = false)
    private int discountValue;

    @Column(nullable = false, name = "start_date")
    private LocalDateTime startDate;

    @Column(nullable = false, name = "end_date")
    private LocalDateTime endDate;

    public Coupon(
            String couponCode,
            CouponType type,
            CouponStatus status,
            int maxQuantity,
            int currentQuantity,
            int discountValue,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        this.couponCode = couponCode;
        this.type = type;
        this.status = status;
        this.maxQuantity = maxQuantity;
        this.currentQuantity = currentQuantity;
        this.discountValue = discountValue;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // 쿠폰 수량 증가 메서드
    public int incerementQuantity() {
        if (currentQuantity < maxQuantity) {
            currentQuantity++;
            return currentQuantity;
        } else {
            throw new IllegalArgumentException("쿠폰이 모두 소진되었 습니다.");
        }
    }
}