package com.sparta.projectblue.domain.hall.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.projectblue.domain.hall.dto.GetHallResponseDto;
import com.sparta.projectblue.domain.hall.dto.GetHallsResponseDto;
import com.sparta.projectblue.domain.hall.entity.Hall;
import com.sparta.projectblue.domain.hall.repository.HallRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HallService {

    private final HallRepository hallRepository;

    public Page<GetHallsResponseDto> getHalls(int page, int size) {

        Pageable pageable = PageRequest.of(page - 1, size);

        return hallRepository.findAll(pageable).map(GetHallsResponseDto::new);
    }

    // 캐싱 적용 대상
    @Cacheable(value = "hall", key = "#id")
    public GetHallResponseDto getHall(Long id) {
        Hall hall = hallRepository.findByIdOrElseThrow(id);
        return new GetHallResponseDto(hall);
    }
}
