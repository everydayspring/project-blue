package com.sparta.projectblue.domain.performance.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.projectblue.domain.hall.entity.Hall;
import com.sparta.projectblue.domain.hall.repository.HallRepository;
import com.sparta.projectblue.domain.performance.dto.*;
import com.sparta.projectblue.domain.performance.entity.Performance;
import com.sparta.projectblue.domain.performance.repository.PerformanceRepository;
import com.sparta.projectblue.domain.poster.entity.Poster;
import com.sparta.projectblue.domain.poster.repository.PosterRepository;
import com.sparta.projectblue.domain.review.entity.Review;
import com.sparta.projectblue.domain.review.repository.ReviewRepository;
import com.sparta.projectblue.domain.round.entity.Round;
import com.sparta.projectblue.domain.round.repository.RoundRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceService {

    private final PerformanceRepository performanceRepository;

    private final HallRepository hallRepository;
    private final ReviewRepository reviewRepository;
    private final RoundRepository roundRepository;
    private final PosterRepository posterRepository;

    private static final String NO_FOUND_PERFORMANCE = "공연을 찾을 수 없습니다.";

    public Page<GetPerformancesResponseDto> getPerformances(int page, int size) {

        Pageable pageable = PageRequest.of(page - 1, size);

        return performanceRepository.findAllPerformance(pageable);
    }

    // 캐싱 적용 대상
    @Cacheable(value = "performance", key = "#id")
    public GetPerformanceResponseDto getPerformance(Long id) {
        Performance performance =
                performanceRepository
                        .findById(id)
                        .orElseThrow(() -> new IllegalArgumentException(NO_FOUND_PERFORMANCE));

        Hall hall =
                hallRepository
                        .findById(performance.getHallId())
                        .orElseThrow(() -> new IllegalArgumentException("공연장의 정보가 없습니다."));

        Poster poster =
                posterRepository
                        .findByPerformanceId(performance.getId())
                        .orElseThrow(() -> new IllegalArgumentException("포스터 정보가 없습니다."));

        return new GetPerformanceResponseDto(performance, hall, poster);
    }

    // 캐싱 적용 대상
    @Cacheable(value = "rounds", key = "#id")
    public GetPerformanceRoundsResponseDto getRounds(Long id) {

        // 공연 id값 검증
        if (performanceRepository.findById(id).isEmpty()) {
            throw new IllegalArgumentException(NO_FOUND_PERFORMANCE);
        }

        // 회차 전체 조회
        List<Round> rounds = new ArrayList<>(roundRepository.findByPerformanceId(id));

        // 회차 날짜정보, 예매 상태만 분리
        List<GetPerformanceRoundsResponseDto.RoundInfo> roundInfos = new ArrayList<>();
        for (Round round : rounds) {
            roundInfos.add(
                    new GetPerformanceRoundsResponseDto.RoundInfo(
                            round.getDate(), round.getStatus()));
        }

        return new GetPerformanceRoundsResponseDto(roundInfos);
    }

    // 캐싱 적용 대상
    @Cacheable(value = "reviews", key = "#id")
    public GetPerformanceReviewsResponseDto getReviews(Long id) {

        // 공연 id값 검증
        if (performanceRepository.findById(id).isEmpty()) {
            throw new IllegalArgumentException(NO_FOUND_PERFORMANCE);
        }

        // 리뷰 전체 조회
        List<Review> reviews = new ArrayList<>(reviewRepository.findByPerformanceId(id));

        // 리뷰 정보 분리
        List<GetPerformanceReviewsResponseDto.ReviewInfo> reviewInfos = new ArrayList<>();
        for (Review review : reviews) {
            reviewInfos.add(
                    new GetPerformanceReviewsResponseDto.ReviewInfo(
                            review.getReviewRate(), review.getContent()));
        }

        return new GetPerformanceReviewsResponseDto(reviewInfos);
    }

    // 캐싱 적용 대상
    @Cacheable(value = "performancePerformers", key = "#id")
    public GetPerformancePerformersResponseDto getPerformers(Long id) {

        if (performanceRepository.findById(id).isEmpty()) {
            throw new IllegalArgumentException(NO_FOUND_PERFORMANCE);
        }

        List<GetPerformancePerformersResponseDto.PerformerInfo> performers =
                performanceRepository.findPerformersByPerformanceId(id);

        return new GetPerformancePerformersResponseDto(performers);
    }
}
