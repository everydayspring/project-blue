package com.sparta.projectblue.domain.search.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.projectblue.domain.hall.entity.Hall;
import com.sparta.projectblue.domain.hall.repository.HallRepository;
import com.sparta.projectblue.domain.performance.dto.GetPerformancesResponseDto;
import com.sparta.projectblue.domain.performance.entity.Performance;
import com.sparta.projectblue.domain.performance.repository.PerformanceRepository;
import com.sparta.projectblue.domain.performer.entity.Performer;
import com.sparta.projectblue.domain.performer.repository.PerformerRepository;
import com.sparta.projectblue.domain.performerperformance.entity.PerformerPerformance;
import com.sparta.projectblue.domain.performerperformance.repository.PerformerPerformanceRepository;
import com.sparta.projectblue.domain.search.document.SearchDocument;
import com.sparta.projectblue.domain.search.dto.KeywordSearchJPAResponseDto;
import com.sparta.projectblue.domain.search.dto.KeywordSearchResponseDto;
import com.sparta.projectblue.domain.search.repository.ESRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private final PerformanceRepository performanceRepository;
    private final ESRepository elasticsearchRepository;

    private final HallRepository hallRepository;
    private final PerformerRepository performerRepository;

    private final PerformerPerformanceRepository performerPerformanceRepository;

    public Page<GetPerformancesResponseDto> searchFilter(
            int page, int size, String performanceNm, String userSelectDay, String performer) {

        Pageable pageable = PageRequest.of(page - 1, size);

        LocalDateTime performanceDay =
                (userSelectDay != null)
                        ? LocalDate.parse(userSelectDay).atTime(LocalTime.NOON)
                        : LocalDateTime.now();

        return performanceRepository.findByCondition(
                pageable, performanceNm, performanceDay, performer);
    }

    public KeywordSearchResponseDto searchKeyword(String keyword, int page, int size) {

        PageRequest pageRequest = PageRequest.of(page, size);

        if (Objects.isNull(keyword) || keyword.trim().isEmpty()) {
            return null;
        }

        Page<Performer> performers = performerRepository.findAllByName(keyword, pageRequest);

        List<Long> performerIds = new ArrayList<>();
        for (Performer performer : performers) {
            performerIds.add(performer.getId());
        }

        Page<Hall> halls = hallRepository.findByNameContaining(keyword, pageRequest);

        List<Long> hallIds = new ArrayList<>();
        for (Hall hall : halls) {
            hallIds.add(hall.getId());
        }

        Page<SearchDocument> searchDocuments =
                elasticsearchRepository
                        .findByPerformanceTitleContainingOrPerformersPerformerIdInOrHallIdIn(
                                keyword, performerIds, hallIds, pageRequest);

        return new KeywordSearchResponseDto(performers, searchDocuments, halls);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void syncDocumentScheduled() {
        try {
            List<SearchDocument> documents = performanceRepository.findForESDocument();
            elasticsearchRepository.saveAll(documents);
            log.info(
                    "Elasticsearch sync completed successfully every 10 minutes with {} documents synced.",
                    documents.size());
        } catch (Exception e) {
            log.error("Elasticsearch sync failed", e);
        }
    }

    public KeywordSearchJPAResponseDto searchKeywordJpa(String keyword, int page, int size) {

        PageRequest pageRequest = PageRequest.of(page, size);

        if (Objects.isNull(keyword) || keyword.trim().isEmpty()) {
            return null;
        }

        Page<Performer> performers = performerRepository.findAllByName(keyword, pageRequest);

        List<Long> performerIds = new ArrayList<>();
        for (Performer performer : performers) {
            performerIds.add(performer.getId());
        }

        Page<Hall> halls = hallRepository.findByNameContaining(keyword, pageRequest);

        List<Long> hallIds = new ArrayList<>();
        for (Hall hall : halls) {
            hallIds.add(hall.getId());
        }

        List<PerformerPerformance> performerPerformances = new ArrayList<>();
        for (Long performerId : performerIds) {
            performerPerformances.addAll(
                    performerPerformanceRepository.findAllByPerformerId(performerId));
        }

        performanceRepository.findByHallIdIn(hallIds);

        Page<Performance> performances =
                performanceRepository.findByTitleContaining(keyword, pageRequest);

        return new KeywordSearchJPAResponseDto(performers, performances, halls);
    }
}
