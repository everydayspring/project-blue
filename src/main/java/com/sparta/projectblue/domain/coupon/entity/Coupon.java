package com.sparta.projectblue.domain.coupon.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sparta.projectblue.domain.common.entity.BaseEntity;
import com.sparta.projectblue.domain.common.enums.CouponStatus;
import com.sparta.projectblue.domain.common.enums.CouponType;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private Long discountValue;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(nullable = false, name = "start_date")
    private LocalDateTime startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(nullable = false, name = "end_date")
    private LocalDateTime endDate;

    @Builder
    public Coupon(
            String couponCode,
            CouponType type,
            CouponStatus status,
            int maxQuantity,
            int currentQuantity,
            Long discountValue,
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
    public int incrementQuantity() {
        if (currentQuantity < maxQuantity) {
            currentQuantity++;
            return currentQuantity;
        } else {
            throw new IllegalArgumentException("쿠폰이 모두 소진되었습니다.");
        }
    }

    // 쿠폰 상태 변경 메서드
    public void usedCoupon() {
        this.status = CouponStatus.USED;
    }
}
