package com.sparta.projectblue.domain.coupon.service;

import com.sparta.projectblue.domain.coupon.repository.CouponRepository;
import com.sparta.projectblue.domain.usedCoupon.repository.UsedCouponRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private UsedCouponRepository usedCouponRepository;

    @InjectMocks
    private CouponService couponService;
}
