package com.sparta.projectblue.domain.round.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sparta.projectblue.domain.common.entity.BaseEntity;
import com.sparta.projectblue.domain.common.enums.PerformanceStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Entity
@Setter
@Table(name = "rounds")
public class Round extends BaseEntity {

    @Column(nullable = false, name = "performance_id")
    private Long performanceId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(nullable = false)
    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PerformanceStatus status;

    public Round(Long performanceId, LocalDateTime date, PerformanceStatus status) {

        this.performanceId = performanceId;
        this.date = date;
        this.status = status;
    }

    public void updateDate(LocalDateTime date) {

        this.date = date;
    }

    public void updateStatus(PerformanceStatus status) {

        this.status = status;
    }
}
