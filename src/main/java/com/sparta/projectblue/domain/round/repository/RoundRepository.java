package com.sparta.projectblue.domain.round.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.projectblue.domain.round.entity.Round;

public interface RoundRepository extends JpaRepository<Round, Long> {

    List<Round> findByPerformanceId(Long performanceId);

    void deleteByPerformanceId(Long performanceId);
}
