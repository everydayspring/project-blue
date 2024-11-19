package com.sparta.projectblue.domain.performer.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.projectblue.config.CacheKey;
import com.sparta.projectblue.domain.performer.dto.GetPerformerResponseDto;
import com.sparta.projectblue.domain.performer.dto.GetPerformersResponseDto;
import com.sparta.projectblue.domain.performer.entity.Performer;
import com.sparta.projectblue.domain.performer.repository.PerformerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformerService {

    private final PerformerRepository performerRepository;

    @Cacheable(value = CacheKey.PERFORMER, key = "#id")
    public GetPerformerResponseDto getPerformer(Long id) {

        Performer performer =
                performerRepository
                        .findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("배우를 찾을 수 없습니다."));

        return new GetPerformerResponseDto(performer);
    }

    public GetPerformersResponseDto getPerformers(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Performer> performerPage = performerRepository.findAll(pageable);

        List<GetPerformersResponseDto.PerformerInfo> performerInfoList =
                performerPage.map(GetPerformersResponseDto.PerformerInfo::new).getContent();

        return new GetPerformersResponseDto(performerInfoList);
    }
}
