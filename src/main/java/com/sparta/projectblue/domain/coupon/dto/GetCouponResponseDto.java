package com.sparta.projectblue.domain.coupon.dto;

import com.sparta.projectblue.domain.common.enums.CouponStatus;
import com.sparta.projectblue.domain.common.enums.CouponType;
import com.sparta.projectblue.domain.coupon.entity.Coupon;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GetCouponResponseDto {

    private final Long id;
    private final String code;
    private final CouponStatus status;
    private final CouponType type;
    private final int maxQuantity;
    private final int currentQuantity;
    private final int discountValue;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;

    public GetCouponResponseDto(Coupon coupon) {

        this.id = coupon.getId();
        this.code = coupon.getCouponCode();
        this.status = coupon.getStatus();
        this.type = coupon.getType();
        this.maxQuantity = coupon.getMaxQuantity();
        this.currentQuantity = coupon.getCurrentQuantity();
        this.discountValue = coupon.getDiscountValue();
        this.startDate = coupon.getStartDate();
        this.endDate = coupon.getEndDate();
    }
}
