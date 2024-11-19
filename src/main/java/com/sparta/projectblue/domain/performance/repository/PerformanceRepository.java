package com.sparta.projectblue.domain.performance.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.projectblue.domain.performance.entity.Performance;

public interface PerformanceRepository
        extends JpaRepository<Performance, Long>, PerformanceQueryRepository {

    List<Performance> findAllById(Long performanceId);

    Page<Performance> findByTitleContaining(String performanceTitle, Pageable pageable);

    List<Performance> findByHallIdIn(List<Long> hallIds);
}
