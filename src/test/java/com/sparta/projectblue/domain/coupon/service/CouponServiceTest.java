package com.sparta.projectblue.domain.coupon.service;

import com.sparta.projectblue.domain.common.dto.AuthUser;
import com.sparta.projectblue.domain.common.enums.CouponStatus;
import com.sparta.projectblue.domain.common.enums.CouponType;
import com.sparta.projectblue.domain.common.enums.UserRole;
import com.sparta.projectblue.domain.coupon.dto.GetCouponResponseDto;
import com.sparta.projectblue.domain.coupon.entity.Coupon;
import com.sparta.projectblue.domain.coupon.repository.CouponRepository;
import com.sparta.projectblue.domain.usedCoupon.repository.UsedCouponRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;


@ExtendWith(SpringExtension.class)
public class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private UsedCouponRepository usedCouponRepository;

    @InjectMocks
    private CouponService couponService;

    @Test
    public void 쿠폰_선착순_발행_성공() {
        // given
        Coupon coupon  = new Coupon("couponCode", CouponType.AMOUNT, CouponStatus.ACTIVE,
                10, 0, 1000L, LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        Long couponId = coupon.getId();

        given(couponRepository.findByIdOrElseThrow(couponId)).willReturn(coupon);
        given(usedCouponRepository.existsByCouponIdAndUserId(couponId, 1L)).willReturn(false);
        given(couponRepository.save(any(Coupon.class))).willReturn(coupon);

        AuthUser authUser = new AuthUser(1L, "test@test.com",
                "testUser", UserRole.ROLE_USER);

        // when
        Coupon response = couponService.firstCoupon(authUser, couponId);

        // then
        assertNotNull(response);
        assertEquals(couponId, response.getId());
        assertEquals(1, coupon.getCurrentQuantity()); // 수량 증가 확인
    }

    @Test
    public void 이미_발급받은_쿠폰(){
        // given
        Coupon coupon  = new Coupon("couponCode", CouponType.AMOUNT, CouponStatus.ACTIVE,
                10, 0, 1000L, LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        Long couponId = coupon.getId();

        given(couponRepository.findByIdOrElseThrow(couponId)).willReturn(coupon);
        given(usedCouponRepository.existsByCouponIdAndUserId(couponId, 1L)).willReturn(true);

        AuthUser authUser = new AuthUser(1L, "test@test.com",
                "testUser", UserRole.ROLE_USER);

        // when, then
        assertThrows(IllegalArgumentException.class, () -> couponService.firstCoupon(authUser, couponId),
                "이미 발급받은 쿠폰입니다.");
    }

    @Test
    public void 쿠폰_소진(){
        // given
        Coupon coupon  = new Coupon("couponCode", CouponType.AMOUNT, CouponStatus.ACTIVE,
                2, 2, 1000L, LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        Long couponId = coupon.getId();

        given(couponRepository.findByIdOrElseThrow(couponId)).willReturn(coupon);
        given(usedCouponRepository.existsByCouponIdAndUserId(couponId, 1L)).willReturn(false);

        AuthUser authUser = new AuthUser(1L, "test@test.com",
                "testUser", UserRole.ROLE_USER);

        // when, then
        assertThrows(IllegalArgumentException.class, () -> couponService.firstCoupon(authUser, couponId),
                "쿠폰이 모두 소진되었습니다.");
    }

    @Test
    public void 쿠폰_단건_조회(){
        // given
        Coupon coupon  = new Coupon("couponCode", CouponType.AMOUNT, CouponStatus.ACTIVE,
                1, 0, 1000L, LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        Long couponId = coupon.getId();

        given(couponRepository.findByIdOrElseThrow(couponId)).willReturn(coupon);

        // when
        GetCouponResponseDto response = couponService.getCoupon(couponId);

        // then
        assertNotNull(response);
        assertEquals(couponId, response.getId());
        assertEquals(coupon.getCouponCode(), response.getCode());
        assertEquals(CouponType.AMOUNT, response.getType());
        assertEquals(CouponStatus.ACTIVE, response.getStatus());
    }

    @Test
    public void 쿠폰_페이징_조회(){
        // given
        List<Coupon>  coupons = List.of(
                new Coupon("couponCode", CouponType.AMOUNT, CouponStatus.ACTIVE,
                        1, 0, 1000L, LocalDateTime.now(), LocalDateTime.now().plusDays(1)),
                new Coupon("couponCode2", CouponType.RATE, CouponStatus.ACTIVE,
                        2, 0, 20L, LocalDateTime.now(), LocalDateTime.now().plusDays(2)));
        Page<Coupon> couponPage = new PageImpl<>(coupons);
        Pageable pageable = PageRequest.of(0, 10);

        given(couponRepository.findAll(pageable)).willReturn(couponPage);

        AuthUser authUser = new AuthUser(1L, "test@test.com",
                "testUser", UserRole.ROLE_USER);
        // when
        Page<GetCouponResponseDto> getCouponResponseDto = couponService.getUserCoupon(authUser, 1, 10);

        // then
        assertNotNull(getCouponResponseDto);
        assertEquals(2, getCouponResponseDto.getTotalElements());
        assertEquals("couponCode", getCouponResponseDto.getContent().get(0).getCode());
        assertEquals("couponCode2", getCouponResponseDto.getContent().get(1).getCode());
    }

    @Test
    public void 쿠폰_사용_성공(){
        // given
        Long couponId = 1L;
        Long originPrice = 10000L;
        Long userId = 1L;
        Long reservationId = 1L;

        Coupon coupon  = new Coupon("couponCode", CouponType.RATE, CouponStatus.ACTIVE,
                1, 0, 50L, LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        given(couponRepository.findByIdOrElseThrow(couponId)).willReturn(coupon);
        // 사용하지 않은 쿠폰
        given(usedCouponRepository.existsByCouponIdAndUserId(couponId, userId)).willReturn(false);

        // when
        Long discountAmount = couponService.useCoupon(couponId, originPrice, userId, reservationId);

        // then
        assertNotNull(discountAmount);
        assertEquals(5000L, discountAmount);
    }

    @Test
    public void 이미_사용된_쿠폰(){
        // given
        Long couponId = 1L;
        Long originPrice = 10000L;
        Long userId = 1L;
        Long reservationId = 1L;

        Coupon coupon  = new Coupon("couponCode", CouponType.RATE, CouponStatus.USED,
                1, 0, 50L, LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        given(couponRepository.findByIdOrElseThrow(couponId)).willReturn(coupon);

        // when, then
        assertThrows(IllegalArgumentException.class, () -> couponService.useCoupon(couponId,
                originPrice, userId, reservationId), "이미 사용된 쿠폰 입니다.");
    }

    @Test
    public void 기간_만료된_쿠폰(){
        // given
        Long couponId = 1L;
        Long originPrice = 10000L;
        Long userId = 1L;
        Long reservationId = 1L;

        Coupon coupon  = new Coupon("couponCode", CouponType.RATE, CouponStatus.EXPIRED,
                1, 0, 50L, LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        given(couponRepository.findByIdOrElseThrow(couponId)).willReturn(coupon);

        // when, then
        assertThrows(IllegalArgumentException.class, () -> couponService.useCoupon(couponId,
                originPrice, userId, reservationId), "기간 만료된 쿠폰 입니다.");
    }

    @Test
    public void 이미_사용한_쿠폰(){
        // given
        Long couponId = 1L;
        Long originPrice = 10000L;
        Long userId = 1L;
        Long reservationId = 1L;

        Coupon coupon  = new Coupon("couponCode", CouponType.RATE, CouponStatus.EXPIRED,
                1, 0, 50L, LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        given(couponRepository.findByIdOrElseThrow(couponId)).willReturn(coupon);
        given(usedCouponRepository.existsByCouponIdAndUserId(couponId, userId)).willReturn(true);

        // when, then
        assertThrows(IllegalArgumentException.class, () -> couponService.useCoupon(couponId,
                originPrice, userId, reservationId), "이미 사용한 쿠폰 입니다.");
    }
}