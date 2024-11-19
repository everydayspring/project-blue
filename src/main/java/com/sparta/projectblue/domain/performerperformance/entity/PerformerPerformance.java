package com.sparta.projectblue.domain.performerperformance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import com.sparta.projectblue.domain.common.entity.BaseEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "performer_performance",
        indexes = {
            @Index(name = "idx_performer_id", columnList = "performer_id"),
            @Index(name = "idx_performance_id", columnList = "performance_id")
        })
public class PerformerPerformance extends BaseEntity {

    @Column(nullable = false, name = "performer_id")
    private Long performerId;

    @Column(nullable = false, name = "performance_id")
    private Long performanceId;

    public PerformerPerformance(Long performerId, Long performanceId) {

        this.performerId = performerId;
        this.performanceId = performanceId;
    }
}
