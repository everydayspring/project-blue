package com.sparta.projectblue.domain.hall.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.sparta.projectblue.domain.hall.dto.GetHallResponseDto;
import com.sparta.projectblue.domain.hall.dto.GetHallsResponseDto;
import com.sparta.projectblue.domain.hall.entity.Hall;
import com.sparta.projectblue.domain.hall.repository.HallRepository;
import com.sparta.projectblue.domain.hall.service.HallService;

@ExtendWith(SpringExtension.class)
class HallServiceTest {

    @Mock private HallRepository hallRepository;

    @InjectMocks private HallService hallService;

    @Test
    void 공연_페이징_조회성공() {
        // given
        List<Hall> halls =
                List.of(
                        new Hall("hallName", "hallAddress", 100),
                        new Hall("hallName2", "hallAddress2", 200));

        Page<Hall> hallPage = new PageImpl<>(halls);

        given(hallRepository.findAll(any(Pageable.class))).willReturn(hallPage);

        // when
        Page<GetHallsResponseDto> responseDto = hallService.getHalls(1, 10);

        // then
        assertNotNull(responseDto);
        assertEquals(2, responseDto.getTotalElements());
        assertEquals("hallName", responseDto.getContent().get(0).getName());
        assertEquals("hallName2", responseDto.getContent().get(1).getName());
    }

    @Test
    void 공연장_단건조회_성공() {
        // given
        Hall hall = new Hall("hallName", "hallAddress", 100);

        given(hallRepository.findByIdOrElseThrow(hall.getId())).willReturn(hall);

        // when
        GetHallResponseDto responseDto = hallService.getHall(hall.getId());

        // then
        assertNotNull(responseDto);
        assertEquals("hallName", responseDto.getName());
        assertEquals("hallAddress", responseDto.getAddress());
        assertEquals(100, responseDto.getSeats());
    }
}
