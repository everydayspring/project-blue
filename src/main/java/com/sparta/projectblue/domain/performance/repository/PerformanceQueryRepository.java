package com.sparta.projectblue.domain.performance.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sparta.projectblue.domain.performance.dto.GetPerformancePerformersResponseDto;
import com.sparta.projectblue.domain.performance.dto.GetPerformancesResponseDto;
import com.sparta.projectblue.domain.search.document.SearchDocument;

public interface PerformanceQueryRepository {

    Page<GetPerformancesResponseDto> findAllPerformance(Pageable pageable);

    Page<GetPerformancesResponseDto> findByCondition(
            Pageable pageable,
            String performanceNm,
            LocalDateTime performanceDay,
            String performer);

    List<GetPerformancePerformersResponseDto.PerformerInfo> findPerformersByPerformanceId(
            Long performanceId);

    List<SearchDocument> findForESDocument();
}
