package com.sparta.projectblue.domain.hall.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.sparta.projectblue.domain.common.dto.AuthUser;
import com.sparta.projectblue.domain.common.enums.UserRole;
import com.sparta.projectblue.domain.hall.dto.CreateHallRequestDto;
import com.sparta.projectblue.domain.hall.dto.CreateHallResponseDto;
import com.sparta.projectblue.domain.hall.dto.UpdateHallRequestDto;
import com.sparta.projectblue.domain.hall.dto.UpdateHallResponseDto;
import com.sparta.projectblue.domain.hall.entity.Hall;
import com.sparta.projectblue.domain.hall.repository.HallRepository;
import com.sparta.projectblue.domain.hall.service.HallAdminService;

@ExtendWith(SpringExtension.class)
class HallAdminServiceTest {

    @Mock private HallRepository hallRepository;

    @InjectMocks private HallAdminService hallAdminService;

    @Test
    void 공연장_생성_성공() {
        // given
        CreateHallRequestDto requestDto = new CreateHallRequestDto("hallName", "hallAddress", 100);

        Hall hall = new Hall("hallName", "hallAddress", 100);

        AuthUser authUser = new AuthUser(1L, "test@test.com", "testUser", UserRole.ROLE_ADMIN);

        given(hallRepository.save(any(Hall.class))).willReturn(hall);

        // when
        CreateHallResponseDto response = hallAdminService.create(authUser, requestDto);

        // then
        assertNotNull(response);
        assertEquals("hallName", response.getName());
        assertEquals("hallAddress", response.getAddress());
        assertEquals(100, response.getSeats());
    }

    @Test
    void 공연장_정보수정_성공() {
        // given
        UpdateHallRequestDto requestDto = new UpdateHallRequestDto("hall2", "Address2", 150);

        Hall hall = new Hall("hallName", "hallAddress", 100);

        AuthUser authUser = new AuthUser(1L, "test@test.com", "testUser", UserRole.ROLE_ADMIN);

        given(hallRepository.findByIdOrElseThrow(hall.getId())).willReturn(hall);
        given(hallRepository.save(any(Hall.class))).willReturn(hall);

        // when
        UpdateHallResponseDto response =
                hallAdminService.update(authUser, hall.getId(), requestDto);

        // then
        assertNotNull(response);
        assertEquals("hall2", response.getName());
        assertEquals("Address2", response.getAddress());
        assertEquals(150, response.getSeats());
        // update 확인
        assertEquals("hall2", hall.getName());
        assertEquals("Address2", hall.getAddress());
        assertEquals(150, response.getSeats());
    }

    @Test
    void 공연장_삭제_성공() {
        // given
        Hall hall = new Hall("hallName", "hallAddress", 100);

        Long hallId = hall.getId();

        AuthUser authUser = new AuthUser(1L, "test@test.com", "testUser", UserRole.ROLE_ADMIN);

        given(hallRepository.findByIdOrElseThrow(hallId)).willReturn(hall);

        // when
        hallAdminService.delete(authUser, hallId);

        // then
        given(hallRepository.findByIdOrElseThrow(hallId))
                .willThrow(new IllegalArgumentException("쿠폰을 찾을 수 없습니다"));

        assertThrows(
                IllegalArgumentException.class, () -> hallRepository.findByIdOrElseThrow(hallId));
    }

    @Test
    void 관리자_권한_없음() {
        // given
        CreateHallRequestDto requestDto = new CreateHallRequestDto("hallName", "hallAddress", 100);

        AuthUser authUser = new AuthUser(1L, "test@test.com", "testUser", UserRole.ROLE_USER);

        // when,then
        assertThrows(
                IllegalArgumentException.class,
                () -> hallAdminService.create(authUser, requestDto),
                "관리자만 접근할 수 있습니다.");
    }

    @Test
    void 공연장_이름등록_예외() {
        // given
        CreateHallRequestDto requestDto = new CreateHallRequestDto("", "hallAddress", 100);

        AuthUser authUser = new AuthUser(1L, "test@test.com", "testUser", UserRole.ROLE_ADMIN);

        // when,then
        assertThrows(
                IllegalArgumentException.class,
                () -> hallAdminService.create(authUser, requestDto),
                "공연장 이름을 등록 해주세요");
    }

    @Test
    void 좌석_0개이하_예외() {
        // given
        CreateHallRequestDto requestDto = new CreateHallRequestDto("hallName", "hallAddress", 0);

        AuthUser authUser = new AuthUser(1L, "test@test.com", "testUser", UserRole.ROLE_ADMIN);

        // when,then
        assertThrows(
                IllegalArgumentException.class,
                () -> hallAdminService.create(authUser, requestDto),
                "좌석 수는 0개 이상이어야 합니다.");
    }

    @Test
    void 공연장_주소등록_예외() {
        // given
        CreateHallRequestDto requestDto = new CreateHallRequestDto("hallName", "", 100);

        AuthUser authUser = new AuthUser(1L, "test@test.com", "testUser", UserRole.ROLE_ADMIN);

        // when,then
        assertThrows(
                IllegalArgumentException.class,
                () -> hallAdminService.create(authUser, requestDto),
                "공연장 주소를 등록 해주세요.");
    }
}
