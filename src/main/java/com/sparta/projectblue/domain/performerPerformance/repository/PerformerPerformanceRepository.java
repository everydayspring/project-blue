package com.sparta.projectblue.domain.performerPerformance.repository;

import com.sparta.projectblue.domain.performerPerformance.entity.PerformerPerformance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerformerPerformanceRepository extends JpaRepository<PerformerPerformance, Long> {
    List<PerformerPerformance> findAllByPerformanceId(Long performanceId);
}