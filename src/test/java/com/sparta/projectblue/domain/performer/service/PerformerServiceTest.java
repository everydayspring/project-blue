package com.sparta.projectblue.domain.performer.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.sparta.projectblue.domain.performer.dto.GetPerformerResponseDto;
import com.sparta.projectblue.domain.performer.dto.GetPerformersResponseDto;
import com.sparta.projectblue.domain.performer.entity.Performer;
import com.sparta.projectblue.domain.performer.repository.PerformerRepository;

@ExtendWith(SpringExtension.class)
class PerformerServiceTest {

    @Mock private PerformerRepository performerRepository;

    @InjectMocks private PerformerService performerService;

    @Test
    void 존재하는_배우를_반환하는_테스트_단건조회() {
        // given
        Performer performer = new Performer("배두훈", "1986-07-15", "KOREA");
        Long performerId = 1L;
        ReflectionTestUtils.setField(performer, "id", performerId);
        when(performerRepository.findById(performerId)).thenReturn(Optional.of(performer));

        // when
        GetPerformerResponseDto result = performerService.getPerformer(performerId);
        // then
        assertNotNull(result);
        assertEquals(performerId, performer.getId());
        assertEquals("배두훈", result.getName());
    }

    @Test
    void 존재하는_배우_리스트를_반환하는_테스트_배우다건조회() {
        // given
        int page = 1;
        int size = 10;
        Pageable pageable = PageRequest.of(page - 1, size);

        Performer performer1 = new Performer("배두훈", "1986-07-15", "KOREA");
        Performer performer2 = new Performer("김태희", "1990-04-22", "KOREA");

        ReflectionTestUtils.setField(performer1, "id", 1L);
        ReflectionTestUtils.setField(performer2, "id", 2L);

        Page<Performer> performerPage =
                new PageImpl<>(List.of(performer1, performer2), pageable, 2);
        when(performerRepository.findAll(any(Pageable.class))).thenReturn(performerPage);

        // when
        GetPerformersResponseDto result = performerService.getPerformers(page, size);

        // then
        assertNotNull(result);
        assertEquals(2, result.getPerformers().size());
        assertEquals("배두훈", result.getPerformers().get(0).getName());
        assertEquals("김태희", result.getPerformers().get(1).getName());
    }

    @Test
    void 존재하지_않는_배우를_조회할_때_예외를_발생시킨다() {

        Long performerId = 1000L;

        when(performerRepository.findById(performerId)).thenReturn(Optional.empty());

        // 예외처리가 발생하지 않고 성공한다면, 예외가 발생해야 합니다라고 출력됨
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> performerService.getPerformer(performerId),
                        "예외가 발생해야 합니다.");
        assertEquals("배우를 찾을 수 없습니다.", exception.getMessage());
    }
}
