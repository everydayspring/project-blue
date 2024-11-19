package com.sparta.projectblue.domain.coupon.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;

import java.time.LocalDateTime;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.sparta.projectblue.domain.common.dto.AuthUser;
import com.sparta.projectblue.domain.common.enums.CouponStatus;
import com.sparta.projectblue.domain.common.enums.CouponType;
import com.sparta.projectblue.domain.common.enums.UserRole;
import com.sparta.projectblue.domain.coupon.dto.CreateCouponRequestDto;
import com.sparta.projectblue.domain.coupon.dto.CreateCouponResponseDto;
import com.sparta.projectblue.domain.coupon.entity.Coupon;
import com.sparta.projectblue.domain.coupon.repository.CouponRepository;

@ExtendWith(SpringExtension.class)
class CouponAdminServiceTest {

    @Mock CouponRepository couponRepository;

    @InjectMocks CouponAdminService couponAdminService;

    @Test
    void 쿠폰_생성_성공() {
        // given
        CreateCouponRequestDto createCouponRequestDto =
                new CreateCouponRequestDto(
                        "couponCode",
                        CouponType.AMOUNT,
                        CouponStatus.ACTIVE,
                        1,
                        0,
                        1000L,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(1));

        Coupon coupon =
                new Coupon(
                        "couponCode",
                        CouponType.AMOUNT,
                        CouponStatus.ACTIVE,
                        1,
                        0,
                        1000L,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(1));

        given(couponRepository.save(any(Coupon.class))).willReturn(coupon);

        AuthUser authUser = new AuthUser(1L, "test@test.com", "testUser", UserRole.ROLE_ADMIN);
        // when
        CreateCouponResponseDto response =
                couponAdminService.create(authUser, createCouponRequestDto);

        // then
        assertEquals(coupon.getCouponCode(), response.getCode());
        assertEquals(coupon.getType(), response.getType());
        assertEquals(coupon.getStatus(), response.getStatus());
        assertEquals(coupon.getDiscountValue(), response.getDiscountValue());
        assertEquals(coupon.getMaxQuantity(), response.getMaxQuantity());
        assertEquals(coupon.getCurrentQuantity(), response.getCurrentQuantity());
        assertEquals(coupon.getStartDate(), response.getStartDate());
        assertEquals(coupon.getEndDate(), response.getEndDate());
    }

    @Test
    void 쿠폰유효_만료일_이후() {
        // given
        CreateCouponRequestDto createCouponRequestDto =
                new CreateCouponRequestDto(
                        "couponCode",
                        CouponType.AMOUNT,
                        CouponStatus.ACTIVE,
                        1,
                        0,
                        1000L,
                        LocalDateTime.now().plusDays(2),
                        LocalDateTime.now());

        AuthUser authUser = new AuthUser(1L, "test@test.com", "testUser", UserRole.ROLE_ADMIN);

        // when, then
        assertThrows(
                IllegalArgumentException.class,
                () -> couponAdminService.create(authUser, createCouponRequestDto),
                "쿠폰 유효일이 쿠폰 만료 이후 일 수 없습니다.");
    }

    @Test
    void 쿠폰_삭제() {
        // given
        Coupon coupon =
                new Coupon(
                        "couponCode",
                        CouponType.AMOUNT,
                        CouponStatus.ACTIVE,
                        1,
                        0,
                        1000L,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(1));

        Long couponId = coupon.getId();

        given(couponRepository.findByIdOrElseThrow(couponId)).willReturn(coupon);

        AuthUser authUser = new AuthUser(1L, "test@test.com", "testUser", UserRole.ROLE_ADMIN);

        // when
        couponAdminService.delete(authUser, couponId);

        // then
        given(couponRepository.findByIdOrElseThrow(couponId))
                .willThrow(new EntityNotFoundException("쿠폰을 찾을 수 업습니다."));

        assertThrows(
                EntityNotFoundException.class,
                () -> couponRepository.findByIdOrElseThrow(couponId),
                "쿠폰을 찾을 수 없습니다.");
    }
}
