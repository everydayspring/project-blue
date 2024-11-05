package com.sparta.projectblue.domain.performer.service;

import com.sparta.projectblue.domain.performer.dto.GetPerformerResponseDto;
import com.sparta.projectblue.domain.performer.dto.GetPerformersResponseDto;
import com.sparta.projectblue.domain.performer.entity.Performer;
import com.sparta.projectblue.domain.performer.repository.PerformerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class PerformerServiceTest {

    @Mock
    private PerformerRepository performerRepository;

    @InjectMocks
    private PerformerService performerService;

    @Test
    void 존재하는_배우를_반환하는_테스트_단건조회() {
        // given
        Performer performer  = new Performer("배두훈", "1986-07-15", "KOREA");
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
        Performer performer1 = new Performer("배두훈", "1986-07-15", "KOREA");
        Performer performer2 = new Performer("김태희", "1990-04-22", "KOREA");

        ReflectionTestUtils.setField(performer1, "id", 1L);
        ReflectionTestUtils.setField(performer2, "id", 2L);

        when(performerRepository.findAll()).thenReturn(List.of(performer1, performer2));

        // when
        GetPerformersResponseDto result = performerService.getPerformers();

        // then
        assertNotNull(result);
        assertEquals(2, result.getPerformers().size()); // 반환된 리스트 크기 검증
        assertEquals("배두훈", result.getPerformers().get(0).getName()); // 첫 번째 배우 이름 검증
        assertEquals("김태희", result.getPerformers().get(1).getName()); // 두 번째 배우 이름 검증

        System.out.println("조회된 배우 리스트:");
        result.getPerformers().forEach(performerInfo -> {
            System.out.println("이름: " + performerInfo.getName());
            System.out.println("생년월일: " + performerInfo.getBirth());
            System.out.println("--------------");
        });
    }

    @Test
    void 존재하지_않는_배우를_조회할_때_예외를_발생시킨다() {

        Long performerId = 1000L;

        when(performerRepository.findById(performerId)).thenReturn(Optional.empty());
        System.out.println("performerRepository.findById 결과: " + performerRepository.findById(performerId));

        // 예외처리가 발생하지 않고 성공한다면, 예외가 발생해야 합니다라고 출력됨
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> performerService.getPerformer(performerId),
                "예외가 발생해야 합니다."
        );
        assertEquals("배우를 찾을 수 없습니다.", exception.getMessage());
        System.out.println("예외 메시지: " + exception.getMessage());
    }


}
